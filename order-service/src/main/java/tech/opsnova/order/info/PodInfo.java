package tech.opsnova.order.info;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Single source of truth for "which pod / version / profile served this request".
 * Identical in shape to catalog-service's PodInfo - feeds both /api/info and the
 * Thymeleaf footer. order-service uses a different default accent so the two UIs
 * are visually distinct in demos.
 */
@Component
public class PodInfo {

    private final String podName;
    private final String podIp;
    private final String nodeName;
    private final String version;
    private final String activeProfile;
    private final String accent;

    public PodInfo(
            Environment environment,
            @Value("${app.version:unknown}") String version,
            @Value("${app.ui.accent:#0d9488}") String accent,
            @Value("${POD_NAME:unknown}") String podName,
            @Value("${POD_IP:unknown}") String podIp,
            @Value("${NODE_NAME:unknown}") String nodeName) {
        this.version = version;
        this.accent = accent;
        this.podName = podName;
        this.podIp = podIp;
        this.nodeName = nodeName;
        String[] active = environment.getActiveProfiles();
        this.activeProfile = active.length == 0 ? "default" : String.join(",", active);
    }

    public String getPodName() {
        return podName;
    }

    public String getPodIp() {
        return podIp;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getVersion() {
        return version;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public String getAccent() {
        return accent;
    }
}
