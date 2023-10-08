/*
 * Copyright 2023 The social-metricbot contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.bmarwell.social.metricbot.bsky.json.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public record AtMentionNotification(
        @JsonProperty("uri") URI uri,
        @JsonProperty("cid") String cid,
        @JsonProperty("author") AtNotificationAuthor author,
        @JsonDeserialize(converter = AtNotificationReasonAdapter.class) @JsonProperty("reason")
                AtNotificationReason reason,
        @JsonProperty("record") AtPostNotificationRecord record,
        @JsonProperty("indexedAt") Instant indexedAt,
        @JsonProperty("isRead") boolean isRead,
        @JsonProperty("embed") Optional<AtEmbed> embed)
        implements AtNotification {}
