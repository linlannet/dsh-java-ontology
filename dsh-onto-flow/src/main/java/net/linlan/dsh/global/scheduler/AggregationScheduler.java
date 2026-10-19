package net.linlan.dsh.global.scheduler;

import net.linlan.dsh.global.service.AggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 汇聚定时调度器，每小时执行一次全量汇聚
 * @author Linlan
 * CreatedAt 2026-10-14
 * @version 1.0
 * @since 1.0
 */
@Component
public class AggregationScheduler {

    private static final Logger log = LoggerFactory.getLogger(AggregationScheduler.class);

    private final AggregationService aggregationService;

    public AggregationScheduler(AggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    /**
     * 每小时全量汇聚
     */
    @Scheduled(fixedRate = 3600000)
    public void scheduledFullAggregation() {
        log.info("Scheduled full aggregation triggered");
        try {
            var stats = aggregationService.scheduledFullAggregation();
            log.info("Scheduled aggregation completed: success={}, {} entities, {} relations in {}ms",
                    stats.success, stats.entityCount, stats.relationCount, stats.durationMs);
        } catch (Exception e) {
            log.error("Scheduled aggregation failed", e);
        }
    }
}
