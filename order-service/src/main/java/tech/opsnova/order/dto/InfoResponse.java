package tech.opsnova.order.dto;

/**
 * Same shape as catalog's /api/info.
 */
public record InfoResponse(
        String podName,
        String podIp,
        String nodeName,
        String version,
        String activeProfile) {
}
