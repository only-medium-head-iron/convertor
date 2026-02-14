package org.demacia.controller;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.demacia.domain.ReceiveRequest;
import org.demacia.receive.ReceiveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author hepenglin
 * @since 2025/3/25 17:18
 **/
@Slf4j
@RestController
@RequestMapping("/api/route/")
public class ThirdPartyApiController {

    @Resource
    private ReceiveService receiveService;

    @PostMapping("{appCode}/**")
    public ResponseEntity<Object> receive(HttpServletRequest httpServletRequest,
                                          @PathVariable("appCode") String appCode,
                                          @RequestHeader Map<String, String> headers,
                                          @RequestParam Map<String, String> queryParams,
                                          @RequestBody String requestBody) {
        StringBuffer requestURL = httpServletRequest.getRequestURL();
        String queryString = httpServletRequest.getQueryString();
        String requestURI = httpServletRequest.getRequestURI();
        String handlerLabel = StrUtil.subAfter(requestURI, "/api/route/" + appCode + "/", true);
        String appKey = queryParams.get("app_key");
        ReceiveRequest receiveRequest = new ReceiveRequest(false, appCode, appKey, queryParams, headers, requestBody);
        String fullRequestURL = requestURL + (StrUtil.isNotBlank(queryString) ? "?" + queryString : "");
        log.info("receive begin full requestURL : {}, receive request: {}", fullRequestURL, receiveRequest);
        Object result = receiveService.receive(receiveRequest);
        log.info("receive end full requestURL : {}, response body: {}", fullRequestURL, result);
        return ResponseEntity.ok(result);
    }
}
