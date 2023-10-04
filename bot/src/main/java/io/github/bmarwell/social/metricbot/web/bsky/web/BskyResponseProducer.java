package io.github.bmarwell.social.metricbot.web.bsky.web;

import io.github.bmarwell.social.metricbot.common.BlueSkyBotConfig;
import io.github.bmarwell.social.metricbot.web.bsky.processing.BskyProcessRequest;
import io.github.bmarwell.social.metricbot.web.bsky.processing.UnprocessedBskyStatusQueueHolder;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Will poll from the Queue and fire an event.
 */
@WebListener
public class BskyResponseProducer implements ServletContextListener, Serializable {

    @Serial
    private static final long serialVersionUID = -4508296428501306541L;

    private static final Logger LOG = LoggerFactory.getLogger(BskyResponseProducer.class);

    @Resource
    private ManagedScheduledExecutorService scheduler;

    @Resource
    private ManagedExecutorService executor;

    @Inject
    private UnprocessedBskyStatusQueueHolder unprocessedBskyStatusQueueHolder;

    @Inject
    private BlueSkyBotConfig bskyConfig;

    @Inject
    private Event<BskyProcessRequest> processEvent;

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        final var scheduledFuture = this.scheduler.scheduleAtFixedRate(
            this::emitMention,
            this.bskyConfig.getPostFinderInitialDelay().getSeconds(),
            // delay here may be short b/c response are usually rare
            2L,
            TimeUnit.SECONDS);

        if (scheduledFuture.isCancelled()) {
            throw new IllegalStateException("Scheduler was canceled: " + scheduledFuture);
        }
    }

    private void emitMention() {
        if (this.unprocessedBskyStatusQueueHolder.isEmpty()) {
            LOG.trace("No BskyStatus to reply to.");
            return;
        }

        final var bskyStatus = this.unprocessedBskyStatusQueueHolder.poll();
        LOG.info(
            "Emitting event for BskyStatus: [{}]/[{}].",
            bskyStatus.cid(),
            bskyStatus.text().replaceAll("\n", "\\\\n"));
        this.processEvent.fireAsync(
            new BskyProcessRequest(bskyStatus),
            NotificationOptions.builder().setExecutor(executor).build()
        );
    }
}
