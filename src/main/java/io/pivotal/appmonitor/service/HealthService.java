package io.pivotal.appmonitor.service;

import io.pivotal.appmonitor.domain.App;
import io.pivotal.appmonitor.model.ActuatorResponse;
import io.pivotal.appmonitor.model.Instance;
import io.pivotal.appmonitor.model.Ping;
import io.pivotal.appmonitor.repository.AppRepository;
import io.pivotal.appmonitor.util.AppConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient.EurekaServiceInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class HealthService {

    final DiscoveryClient discoveryClient;

    final AppRepository appRepository;

    final RestTemplate restTemplate;

    @Value("#{'${monitor}'.split(',')}")
    private List<String> monitorApps;

    @Autowired
    public HealthService(DiscoveryClient discoveryClient, AppRepository appRepository, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.appRepository = appRepository;
        this.restTemplate = restTemplate;
    }


    public ResponseEntity<App> serviceInstancesByApplicationName(
            String applicationName) {
        log.info("Getting Instance details for " + applicationName);
        App app = null;
        try {
            app = new App();
            app.setAppName(applicationName);
            app.setCreatedAt(new Date());

            List<ServiceInstance> serviceInstances = this.discoveryClient.getInstances(applicationName);

            if (!CollectionUtils.isEmpty(serviceInstances)) {
                for (ServiceInstance serviceInstance : serviceInstances) {
                    EurekaServiceInstance eurekaServiceInstance = (EurekaServiceInstance) serviceInstance;

                    Instance instance = new Instance(eurekaServiceInstance.getMetadata().get(AppConstants.instanceId),
                            eurekaServiceInstance.getInstanceInfo().getStatus().toString(),
                            new Date(eurekaServiceInstance.getInstanceInfo().getLastUpdatedTimestamp()));
                    app.getInstanceList().add(instance);
                }
                app.setInstanceCount(serviceInstances.size());
                app.setHealth(getActuatorHealth(applicationName));
            } else {
                log.warn("No Instance details for " + applicationName);
            }

            log.info("Persisting into DB..");
            app = appRepository.save(app);
        } catch (Exception ex) {
            log.error("Error getting Instance details: " + ex.getMessage());
        }
        return new ResponseEntity<>(app, HttpStatus.OK);
    }

    private String getActuatorHealth(String applicationName) {
        log.info("Getting Actuator health..");
        StringBuilder url = new StringBuilder();
        url.append(AppConstants.http)
                .append("://")
                .append(applicationName)
                .append(AppConstants.health_uri);
        ResponseEntity<ActuatorResponse> health = restTemplate.getForEntity(url.toString(), ActuatorResponse.class);
        if (Objects.nonNull(health) &&
                StringUtils.equalsIgnoreCase(AppConstants.up, health.getBody().getStatus()))
            return AppConstants.up;
        else
            return AppConstants.down;
    }

    public ResponseEntity<List<App>> serviceInstances() {
        log.info("Getting Instance details for all apps..");
        List<App> apps = new ArrayList<>();
        if (!CollectionUtils.isEmpty(monitorApps)) {
            for (String monitorApp : monitorApps) {
                apps.add(serviceInstancesByApplicationName(monitorApp).getBody());
            }
        }
        return new ResponseEntity<>(apps, HttpStatus.OK);
    }

    public ResponseEntity<App> latestHealthByApplicationName(String applicationName) {
        log.info("Getting latestHealth for " + applicationName);
        List<App> apps = appRepository.findByAppNameOrderByCreatedAtDesc(applicationName);
        if (!CollectionUtils.isEmpty(apps)) {
            return new ResponseEntity<>(apps.get(0), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<List<App>> latestHealth() {
        log.info("Getting latestHealth for all apps..");
        List<App> apps = new ArrayList<>();
        if (!CollectionUtils.isEmpty(monitorApps)) {
            for (String monitorApp : monitorApps) {
                apps.add(latestHealthByApplicationName(monitorApp).getBody());
            }
        }
        return new ResponseEntity<>(apps, HttpStatus.OK);
    }

    public ResponseEntity<List<Ping>> ping() {
        log.info("Getting latest basic health for all apps..");
        List<Ping> pingList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(monitorApps)) {
            for (String monitorApp : monitorApps) {
                App app = latestHealthByApplicationName(monitorApp).getBody();
                if(Objects.nonNull(app))
                    pingList.add(new Ping(app.getAppName(), app.getHealth(), app.getCreatedAt()));
            }
        }
        return new ResponseEntity<>(pingList, HttpStatus.OK);
    }
}

