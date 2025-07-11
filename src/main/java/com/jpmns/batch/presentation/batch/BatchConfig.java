package com.jpmns.batch.presentation.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jpmns.batch.domain.models.NotificationEventModel;
import com.jpmns.batch.domain.services.interfaces.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.store.MessageGroupStore;
import org.springframework.integration.store.SimpleMessageStore;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Configuration
@EnableIntegration
public class BatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(BatchConfig.class);
    private final NotificationService service;
    private final TaskExecutor taskExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RabbitTemplate rabbitTemplate;

    @Value("${batch.group.timeout}")
    private long groupTimeout;

    @Value("${batch.group.size}")
    private int groupSize;

    @Value("${batch.queue.name}")
    private String queueName;

    @Value("${batch.queue.dlq.name}")
    private String dlqQueueName;

    public BatchConfig(
            @Qualifier("taskExecutor") TaskExecutor taskExecutor,
            NotificationService service,
            RabbitTemplate rabbitTemplate) {
        this.taskExecutor = taskExecutor;
        this.service = service;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Bean
    public SimpleMessageListenerContainer listenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName);
        container.setAcknowledgeMode(AcknowledgeMode.AUTO);
        container.setDefaultRequeueRejected(false);
        return container;
    }

    @Bean
    public IntegrationFlow listener(SimpleMessageListenerContainer listenerContainer) {
        MessageGroupStore messageGroupStore = new SimpleMessageStore();

        return IntegrationFlow
                .from(Amqp.inboundAdapter(listenerContainer)
                        .autoStartup(true))
                .channel(c -> c.executor(taskExecutor))
                .aggregate(a -> a
                        .correlationStrategy(m -> "chunk")
                        .releaseStrategy(g -> g.size() >= groupSize)
                        .expireGroupsUponCompletion(true)
                        .groupTimeout(groupTimeout)
                        .messageStore(messageGroupStore)
                        .sendPartialResultOnExpiry(true))
                .channel(c -> c.executor(taskExecutor))
                .handle((GenericHandler<List<byte[]>>) (payload, headers) -> {
                    CompletableFuture<?> allFutures = CompletableFuture.allOf(payload
                            .stream()
                            .map(bytes -> CompletableFuture.runAsync(() -> {
                                String json = new String(bytes);
                                try {
                                    NotificationEventModel model = objectMapper.readValue(json, NotificationEventModel.class);
                                    service.sendNotification(model);
                                } catch (Exception e) {
                                    logger.error("Error processing notification event", e);

                                    ObjectNode errorNode = JsonNodeFactory.instance.objectNode();

                                    errorNode.put("payload", json);
                                    errorNode.put("errorMessage", e.getMessage());
                                    StringWriter sw = new StringWriter();
                                    e.printStackTrace(new PrintWriter(sw));
                                    errorNode.put("stackTrace", sw.toString());
                                    String jsonError = errorNode.toString();
                                    rabbitTemplate.convertAndSend("", dlqQueueName, jsonError);
                                }
                            }, taskExecutor))
                            .toArray(CompletableFuture[]::new));

                    allFutures.join();

                    return null;
                })
                .get();
    }
}
