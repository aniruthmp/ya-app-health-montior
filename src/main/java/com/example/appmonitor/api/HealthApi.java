package com.example.appmonitor.api;

import com.example.appmonitor.service.HealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class HealthApi {

    @Autowired
    HealthService healthService;

    @GetMapping("/service-instances/{applicationName}")
    public ResponseEntity<List<ServiceInstance>> serviceInstancesByApplicationName(
            @PathVariable("applicationName") String applicationName) {
        log.info("applicationName: " + applicationName);
        return healthService.serviceInstancesByApplicationName(applicationName);
    }
}
