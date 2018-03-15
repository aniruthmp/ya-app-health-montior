package io.pivotal.appmonitor.api;

import io.pivotal.appmonitor.domain.App;
import io.pivotal.appmonitor.model.Ping;
import io.pivotal.appmonitor.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HealthApi {

    final HealthService healthService;

    @Autowired
    public HealthApi(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/ondemand/health/{applicationName}")
    public ResponseEntity<App> serviceInstancesByApplicationName(
            @PathVariable("applicationName") String applicationName) {
        return healthService.serviceInstancesByApplicationName(applicationName);
    }

    @GetMapping("/ondemand/health")
    public ResponseEntity<List<App>> serviceInstances() {
        return healthService.serviceInstances();
    }

    @GetMapping("/latest/health/{applicationName}")
    public ResponseEntity<App> latestHealthByApplicationName(
            @PathVariable("applicationName") String applicationName) {
        return healthService.latestHealthByApplicationName(applicationName);
    }

    @GetMapping("/latest/health")
    public ResponseEntity<List<App>> latestHealth() {
        return healthService.latestHealth();
    }

    @GetMapping("/ping")
    public ResponseEntity<List<Ping>> ping() {
        return healthService.ping();
    }

}
