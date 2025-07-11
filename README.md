# Batch Notification

### Contexto

Uma das estratégias mais eficazes para impulsionar as vendas no e-commerce é o envio de notificações personalizadas aos clientes. Essas notificações podem conter promoções exclusivas, lançamentos de novos produtos, descontos por tempo limitado e recomendações personalizadas com base no histórico de navegação ou de compras do usuário. Ao manter um canal de comunicação direto e relevante com o consumidor, a loja online aumenta significativamente as chances de conversão, além de fortalecer o relacionamento com a marca. A partir disso, foi criado o Batch Notification para o envio de notificações não transacionais.

---

### Arquitetura

Para a execução do Batch Notification, são necessários dois serviços:

- **Trigger**: Inicia o serviço Task.  
- **Task**: Consome outros serviços para montar os eventos que serão enviados ao Batch Notification.  
- **Custom Notification**: Gerencia os templates utilizados pelo Batch Notification.

<img width="803" height="382" src="https://github.com/user-attachments/assets/2d0a9ce7-2e80-49e4-a694-1dd8f9bae619" />

Cada mensagem conterá as seguintes informações:

```json
{
  "channel": "",
  "name": "",
  "data": {},
  "userId": "",
  "target": ""
}
````

* `channel`: Canal de envio da mensagem (SMS, EMAIL, etc.).
* `name`: Nome do template que será utilizado.
* `data`: Objeto contendo os placeholders que serão substituídos no template.
* `userId`: ID do usuário que receberá a notificação.
* `target`: Alvo que receberá a notificação (e-mail, número de telefone, etc.).

---

### Estrutura do Código

O projeto foi dividido em duas camadas:

* **domain**: Contém as regras de negócio da aplicação.
* **presentation**: Contém a forma como a aplicação será acessada.

---

### Modelos de Dados

Para cada canal existente, existe uma tabela que armazena as informações do seu template. Para esta aplicação, foram criados dois canais: `EMAIL` e `SMS`.

#### Tabela: `email_templates`

| Coluna       | Tipo           | Restrições                                 | Descrição                                 |
| ------------ | -------------- | ------------------------------------------ | ----------------------------------------- |
| `id`         | `UUID`         | `PRIMARY KEY`, `DEFAULT gen_random_uuid()` | Identificador único do template de e-mail |
| `name`       | `VARCHAR(100)` | `NOT NULL`                                 | Nome do template                          |
| `subject`    | `VARCHAR(200)` | `NOT NULL`                                 | Assunto do e-mail                         |
| `body_html`  | `TEXT`         | `NOT NULL`                                 | Conteúdo HTML do e-mail                   |
| `version`    | `INTEGER`      | `NOT NULL`, `UNIQUE`                       | Versão do template                        |
| `active`     | `BOOLEAN`      | `NOT NULL`                                 | Indica se o template está ativo           |
| `variables`  | `TEXT[]`       |                                            | Lista de variáveis utilizadas             |
| `created_at` | `TIMESTAMP`    | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP`    | Data de criação                           |
| `updated_at` | `TIMESTAMP`    | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP`    | Data da última atualização                |

#### Tabela: `sms_templates`

| Coluna       | Tipo           | Restrições                                 | Descrição                              |
| ------------ | -------------- | ------------------------------------------ | -------------------------------------- |
| `id`         | `UUID`         | `PRIMARY KEY`, `DEFAULT gen_random_uuid()` | Identificador único do template de SMS |
| `name`       | `VARCHAR(100)` | `NOT NULL`                                 | Nome do template                       |
| `content`    | `TEXT`         | `NOT NULL`                                 | Conteúdo do SMS                        |
| `version`    | `INTEGER`      | `NOT NULL`, `UNIQUE`                       | Versão do template                     |
| `active`     | `BOOLEAN`      | `NOT NULL`                                 | Indica se o template está ativo        |
| `variables`  | `TEXT[]`       |                                            | Lista de variáveis utilizadas          |
| `created_at` | `TIMESTAMP`    | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP`    | Data de criação                        |
| `updated_at` | `TIMESTAMP`    | `NOT NULL`, `DEFAULT CURRENT_TIMESTAMP`    | Data da última atualização             |

O serviço **Custom Notification** gerenciará essas tabelas. Cada tabela possui um índice nos campos `active` e `version`. Com isso, apenas uma versão poderá estar ativa por vez, o que aumenta a velocidade das consultas.

---

### Design Patterns

Foram utilizados os padrões **Strategy** e **Factory**:

* **Strategy**: Foi utilizado para facilitar o encapsulamento das regras de negócio de cada tipo de envio de notificação. Assim, é possível definir como a notificação será enviada, qual tipo de retry será aplicado e quais validações serão feitas. Isso também facilita a adição de novos fluxos, sem a necessidade de refatorar regras existentes, preservando o princípio OCP do SOLID.
* **Factory**: Encapsula a lógica de criação das estratégias (strategies), facilitando o reuso e a manutenção do código.

---

### Multithreading

Para multithreading, foi utilizado o `ThreadPoolTaskExecutor`, que configura um pool de threads responsável pelo envio das notificações. Na configuração, foi definido o número de núcleos \* 2, já que se tratam de tarefas bloqueantes e essa abordagem evita a ociosidade das threads.

O número máximo de threads foi definido para permitir a criação de threads adicionais, se necessário. Já a `queue capacity` foi configurada como número de núcleos \* 1000, para criar uma fila suficientemente grande e proporcional à capacidade do hardware, evitando a rejeição precoce de tarefas em momentos de pico de carga.

---

### Retry

Foi utilizada a biblioteca **Spring Retry**, com a estratégia padrão de retry, **sem o backoff exponencial**, considerando que os serviços chamados para o envio das notificações já devem possuir circuit breaker configurado.

---

### CI

Foi adicionada uma pipeline que executa os testes da aplicação. No entanto, entende-se que um ciclo completo de CI deve conter as seguintes quatro etapas:

* **Lint**: Verificação de formatação e padrões do código.
* **Test**: Execução dos testes unitários e de integração.
* **Quality**: Análise do código por uma ferramenta de qualidade.
* **Build**: Geração da imagem de deploy.

---

### Cache

Foi utilizado cache para armazenamento dos templates. Com isso, a máquina responsável pelo banco de dados não precisa ser dimensionada para suportar leituras frequentes desses dados, reduzindo a carga no banco, melhorando a performance da aplicação e também preservando custos.

---

### Uso de Memória

Na configuração do projeto, foi definido que todos os batches de mensagens ficarão alocados na fila. Por isso, é de suma importância atentar-se à configuração da máquina onde a aplicação será executada.

---

### Execução

Para executar a aplicação, clone o repositório:

```sh
git clone https://github.com/JPedro109/batch-notification
```

Em seguida, execute o comando abaixo:

```sh
docker-compose up -d
```

Após os containers, vá até esse host
```sh
http://localhost:15672/#/
```

Agora siga os seguintes passos:
- Logue-se com user = guest e password = guest
- Vá até a aba Queues and Streams
- Acesse a queue notification

Agora você pode testar a aplicação utilizado esse dois bodies:

```sh
{
    "channel": "SMS",
    "name": "PROMOTION",
    "data": {
        "name": "Test",
        "link": "https://domain.com/publish"
    },
    "userId": "10128862-a940-465e-93ab-e7627651eab9",
    "target": "11999999999"
}
```

```sh
{
    "channel": "EMAIL",
    "name": "PROMOTION",
    "data": {
        "name": "Test",
        "link": "https://domain.com/publish"
    },
    "userId": "10128862-a940-465e-93ab-e7627651eab9",
    "target": "email@test.com"
}
```
---

### Observação

- Não foram adicionadas lógicas de envio das notificações pois entende-se que não era o intuito.
- Se a aplicação for reiniciado a variável de ambiente SPRING_SQL_INIT_MODE, deve ser modificado para never.
