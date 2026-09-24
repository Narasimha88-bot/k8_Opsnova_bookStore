package tech.opsnova.catalog.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * [TEACHING HOOKS] Deliberately unsecured and deliberately fragile - they exist
 * so the instructor can break the app live. Do NOT secure or "fix" these. They
 * are open in every profile (also permitted in the full security config).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final ApplicationEventPublisher publisher;
    private final ApplicationAvailability availability;
    // Retains allocated memory so the GC can never reclaim it.
    private final List<byte[]> retained = new ArrayList<>();

    public AdminController(ApplicationEventPublisher publisher, ApplicationAvailability availability) {
        this.publisher = publisher;
        this.availability = availability;
    }

    /**
     * Flips an in-memory readiness flag. Forces /actuator/health/readiness DOWN so
     * Kubernetes pulls the pod from the Service endpoints WITHOUT killing it
     * (liveness stays UP). Call again to restore.
     */
    @PostMapping("/toggle-readiness")
    public Map<String, Object> toggleReadiness() {
        ReadinessState next = availability.getReadinessState() == ReadinessState.ACCEPTING_TRAFFIC
                ? ReadinessState.REFUSING_TRAFFIC
                : ReadinessState.ACCEPTING_TRAFFIC;
        AvailabilityChangeEvent.publish(publisher, this, next);
        log.warn("[TEACHING HOOK] readiness toggled -> {}", next);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("readinessState", next.toString());
        body.put("ready", next == ReadinessState.ACCEPTING_TRAFFIC);
        return body;
    }

    /**
     * Allocates and RETAINS N megabytes. Call repeatedly to cross the container
     * memory limit and force an OOMKill (the JVM-heap-vs-container-limit lesson).
     */
    @GetMapping("/consume-memory")
    public Map<String, Object> consumeMemory(@RequestParam int mb) {
        if (mb < 1 || mb > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mb must be between 1 and 2000");
        }
        byte[] block = new byte[mb * 1024 * 1024];
        Arrays.fill(block, (byte) 1); // touch the pages so they are actually committed
        retained.add(block);

        long retainedMb = retained.stream().mapToLong(b -> b.length).sum() / (1024 * 1024);
        Runtime rt = Runtime.getRuntime();
        log.warn("[TEACHING HOOK] consumed {} MB, retained total {} MB (JVM max {} MB)",
                mb, retainedMb, rt.maxMemory() / (1024 * 1024));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("allocatedMb", mb);
        body.put("retainedTotalMb", retainedMb);
        body.put("jvmMaxMb", rt.maxMemory() / (1024 * 1024));
        return body;
    }
}
