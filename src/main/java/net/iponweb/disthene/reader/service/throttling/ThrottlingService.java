package net.iponweb.disthene.reader.service.throttling;

import com.google.common.util.concurrent.RateLimiter;
import net.iponweb.disthene.reader.config.ThrottlingConfiguration;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrei Ivanov
 */
public class ThrottlingService {
    final static Logger logger = Logger.getLogger(ThrottlingService.class);


    private ThrottlingConfiguration throttlingConfiguration;
    private Map<String, RateLimiter> rateLimiters = new HashMap<>();
    private RateLimiter totalRateLimiter;

    public ThrottlingService(ThrottlingConfiguration throttlingConfiguration) {
        this.throttlingConfiguration = throttlingConfiguration;

        for (Map.Entry<String, Integer> entry : throttlingConfiguration.getTenants().entrySet()) {
            rateLimiters.put(entry.getKey(), RateLimiter.create(entry.getValue()));
        }

        totalRateLimiter = RateLimiter.create(throttlingConfiguration.getTotalQPS());
    }

    public double throttle(String tenant) {
        if (!throttlingConfiguration.isThrottlingEnabled()) {
            return 0;
        }

        if (throttlingConfiguration.getExceptions().contains(tenant)) {
            return 0;
        }

        if (!rateLimiters.containsKey(tenant)) {
            rateLimiters.put(tenant, RateLimiter.create(throttlingConfiguration.getDefaultQPS()));
        }

        double tenantThrottled = rateLimiters.get(tenant).acquire();

        double totalThrottled = totalRateLimiter.acquire();

        if (tenantThrottled > 0) {
            logger.debug("Tenant " + tenant + " was throttled for " + tenantThrottled + " seconds");
        }

        if (totalThrottled > 0) {
            logger.debug("Total " + tenant + " was throttled for " + totalThrottled + " seconds");
        }

        return tenantThrottled + totalThrottled;
    }

    public void reload(ThrottlingConfiguration throttlingConfiguration) {
        Map<String, RateLimiter> rateLimiters = new HashMap<>();
        for (Map.Entry<String, Integer> entry : throttlingConfiguration.getTenants().entrySet()) {
            rateLimiters.put(entry.getKey(), RateLimiter.create(entry.getValue()));
        }

        RateLimiter totalRateLimiter = RateLimiter.create(throttlingConfiguration.getTotalQPS());

        this.throttlingConfiguration = throttlingConfiguration;
        this.rateLimiters = rateLimiters;
        this.totalRateLimiter = totalRateLimiter;

    }
}
