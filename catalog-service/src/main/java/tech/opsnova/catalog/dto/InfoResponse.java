package tech.opsnova.catalog.dto;

/**
 * Shape of GET /api/info. Used constantly in class to show which pod served a
 * request. podName / podIp / nodeName come from the Kubernetes Downward API and
 * fall back to "unknown" when the app runs outside Kubernetes.
 */
public record InfoResponse(
        String podName,
        String podIp,
        String nodeName,
        String version,
        String activeProfile) {
}
