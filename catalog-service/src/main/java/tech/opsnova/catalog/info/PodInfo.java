package tech.opsnova.catalog.info;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Single source of truth for "which pod / version / profile served this request".
 * Used by BOTH the JSON endpoint (/api/info) and the Thymeleaf footer, so the UI
 * and the API can never disagree. Referenced directly in templates as
 * {@code ${@podInfo.*}}.
 *
 * The pod fields come from the Kubernetes Downward API and fall back to
 * "unknown" when unset (e.g. plain `docker run` or an IDE).
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
            @Value("${app.ui.accent:#4f46e5}") String accent,
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
