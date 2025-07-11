package com.jpmns.batch.domain.models;

import java.util.Map;

public record NotificationEventModel(String channel, String name, String userId, String target, Map<String, String> data) { }
