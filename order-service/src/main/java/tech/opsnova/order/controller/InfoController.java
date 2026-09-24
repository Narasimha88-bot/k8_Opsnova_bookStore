package tech.opsnova.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.opsnova.order.dto.InfoResponse;
import tech.opsnova.order.info.PodInfo;

/**
 * GET /api/info - same shape as catalog. Backed by the shared PodInfo bean.
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
