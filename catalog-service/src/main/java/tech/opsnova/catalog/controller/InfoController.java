package tech.opsnova.catalog.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.opsnova.catalog.dto.InfoResponse;
import tech.opsnova.catalog.info.PodInfo;

/**
 * GET /api/info - shows which pod, version and profile served the request.
 * Backed by the shared {@link PodInfo} bean (same data the UI footer uses).
 */
@RestController
public class InfoController {

    private final PodInfo podInfo;

    public InfoController(PodInfo podInfo) {
        this.podInfo = podInfo;
    }

    @GetMapping("/api/info")
    public InfoResponse info() {
        return new InfoResponse(
                podInfo.getPodName(),
                podInfo.getPodIp(),
                podInfo.getNodeName(),
                podInfo.getVersion(),
                podInfo.getActiveProfile());
    }
}
