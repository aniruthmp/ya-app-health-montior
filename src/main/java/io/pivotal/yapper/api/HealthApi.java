package io.pivotal.yapper.api;

import io.pivotal.yapper.domain.App;
import io.pivotal.yapper.model.EmailMessage;
import io.pivotal.yapper.model.Ping;
import io.pivotal.yapper.model.SlackMessage;
import io.pivotal.yapper.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class HealthApi {

    private final HealthService healthService;

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

    @PostMapping(path = "/slack", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> sendSlack(@RequestBody SlackMessage slackMessage) {
        return healthService.sendSlack(slackMessage);
    }

    @PostMapping(path = "/email", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> email(@RequestBody EmailMessage emailMessage) {
        return healthService.email(emailMessage);
    }
}
