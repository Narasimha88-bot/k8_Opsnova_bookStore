package tech.opsnova.order.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * [TEACHING HOOK] Sleeps app.startup.delay.seconds (env APP_STARTUP_DELAY_SECONDS,
 * default 0) before the app finishes starting, keeping readiness DOWN for the
 * delay. Drives the startupProbe session. Identical to catalog's.
 */
@Component
public class StartupDelay implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupDelay.class);

    @Value("${app.startup.delay.seconds:0}")
    private long delaySeconds;

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        if (delaySeconds > 0) {
            log.warn("[TEACHING HOOK] startup delay: sleeping {}s before marking started...", delaySeconds);
            Thread.sleep(delaySeconds * 1000L);
            log.warn("[TEACHING HOOK] startup delay complete.");
        }
    }
}
