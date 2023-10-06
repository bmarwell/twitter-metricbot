package io.github.bmarwell.social.metricbot.bsky.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.bmarwell.social.metricbot.bsky.RecordType;

public record AtNotificationRepostRecord(@JsonProperty("$type") RecordType type) implements AtNotificationRecord {}
