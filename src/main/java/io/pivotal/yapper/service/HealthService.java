package io.pivotal.yapper.service;

import io.pivotal.yapper.domain.App;
import io.pivotal.yapper.model.*;
import io.pivotal.yapper.repository.AppRepository;
import io.pivotal.yapper.util.AppConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient.EurekaServiceInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@RefreshScope
@Slf4j
public class HealthService {

    private final DiscoveryClient discoveryClient;
    private final AppRepository appRepository;
    private final RestTemplate restTemplate;
    private final JavaMailSender mailSender;

    @Value("#{'${monitor}'.split(',')}")
    private List<String> monitorApps;

    @Autowired
    public HealthService(DiscoveryClient discoveryClient, AppRepository appRepository,
                         RestTemplate restTemplate, JavaMailSender mailSender) {
        this.discoveryClient = discoveryClient;
        this.appRepository = appRepository;
        this.restTemplate = restTemplate;
        this.mailSender = mailSender;
    }

    public ResponseEntity<App> serviceInstancesByApplicationName(
            String applicationName) {
        log.info("Getting Instance details for " + applicationName);
        App app = new App();
        try {
            app.setAppName(applicationName);
            app.setCreatedAt(new Date());
            ActuatorHealth actuatorHealth = new ActuatorHealth();
            actuatorHealth.setStatus(AppConstants.unknown);
            app.setHealth(actuatorHealth);

            List<ServiceInstance> serviceInstances;
            serviceInstances = this.discoveryClient.getInstances(applicationName);

            if (!isEmpty(serviceInstances)) {
                for (ServiceInstance serviceInstance : serviceInstances) {
                    EurekaServiceInstance eurekaServiceInstance = (EurekaServiceInstance) serviceInstance;
                    log.info(eurekaServiceInstance.toString());

                    Instance instance = new Instance(eurekaServiceInstance.getMetadata().get(AppConstants.instanceId),
                            eurekaServiceInstance.getInstanceInfo().getStatus().toString(),
                            new Date(eurekaServiceInstance.getInstanceInfo().getLastUpdatedTimestamp()));
                    app.getInstances().add(instance);
                }
                app.setInstanceCount(serviceInstances.size());
                app.setHealth(getActuatorHealth(applicationName));
            } else {
                log.warn("No Instance details for " + applicationName);
            }

            log.info("Persisting into DB..");
            app = appRepository.save(app);
        } catch (Exception ex) {
            log.error("Error Persisting into DB: " + ex.getMessage());
        }
        sendNotification(app);
        return new ResponseEntity<>(app, HttpStatus.OK);
    }

    private ActuatorHealth getActuatorHealth(String applicationName) {
        log.info("Getting Actuator health..");
        ActuatorHealth actuatorActuatorHealth = new ActuatorHealth();
        actuatorActuatorHealth.setStatus(AppConstants.unknown);
        StringBuilder url = new StringBuilder();
        url.append(AppConstants.http)
                .append("://")
                .append(applicationName)
                .append(AppConstants.health_uri);
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(url.toString(), String.class);
            if (Objects.nonNull(responseEntity)) {
                actuatorActuatorHealth.setDetails(extractErrorDetails(responseEntity.getBody()));
            }
        } catch (Exception ex) {
            log.error("Error in getActuatorHealth: " + ex.getMessage());
        }
        return actuatorActuatorHealth;
    }

    private Map<String, Object> extractErrorDetails(String healthJson) throws IOException {
        log.debug("Came inside extractErrorDetails for " + healthJson);
        HashMap<String, Object> details = new ObjectMapper().readValue(healthJson, HashMap.class);

        //Iterate now
        HashMap<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : details.entrySet()) {
            if (StringUtils.equalsIgnoreCase(AppConstants.linked_hashmap, entry.getValue().getClass().getName())) {
                HashMap<String, Object> innerMap = (HashMap<String, Object>) entry.getValue();
                innerMap.forEach((k, v) -> {
                    if ("status".equalsIgnoreCase(k) &&
                            AppConstants.down.equalsIgnoreCase(Objects.toString(v, "")))
                        resultMap.put(entry.getKey(), entry.getValue());
                });
            }
        }
        log.debug("extractErrorDetails returns: " + resultMap);
        return resultMap;
    }

    public ResponseEntity<List<App>> serviceInstances() {
        log.info("Getting Instance details for all apps..");
        List<App> apps = new ArrayList<>();
        if (!isEmpty(monitorApps)) {
            log.info("Getting details for " + monitorApps.toString());
            monitorApps.parallelStream().forEach(monitorApp ->
                    apps.add(serviceInstancesByApplicationName(monitorApp).getBody()));
        }
        return new ResponseEntity<>(apps, HttpStatus.OK);
    }

    public ResponseEntity<App> latestHealthByApplicationName(String applicationName) {
        log.info("Getting latestHealth for " + applicationName);
        List<App> apps = appRepository.findByAppNameOrderByCreatedAtDesc(applicationName);
        if (!isEmpty(apps)) {
            return new ResponseEntity<>(apps.get(0), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<List<App>> latestHealth() {
        log.info("Getting latestHealth for all apps..");
        List<App> apps = new ArrayList<>();
        if (!isEmpty(monitorApps)) {
            for (String monitorApp : monitorApps) {
                apps.add(latestHealthByApplicationName(monitorApp).getBody());
            }
        }
        return new ResponseEntity<>(apps, HttpStatus.OK);
    }

    public ResponseEntity<List<Ping>> ping() {
        log.info("Getting latest basic health for all apps..");
        List<Ping> pingList = new ArrayList<>();
        if (!isEmpty(monitorApps)) {
            for (String monitorApp : monitorApps) {
                App app = latestHealthByApplicationName(monitorApp).getBody();
                if (Objects.nonNull(app))
                    pingList.add(new Ping(app.getAppName(), app.getHealth().getStatus(), app.getCreatedAt()));
            }
        }
        return new ResponseEntity<>(pingList, HttpStatus.OK);
    }

    private void sendNotification(App app) {
        log.info("Came inside sendNotification for " + app);
        String healthStatus = Objects.toString(app.getHealth().getStatus(), AppConstants.unknown);
        if (!StringUtils.equalsIgnoreCase(Status.UP.toString(), healthStatus)) {
            ResponseEntity<String> slackNotified = sendSlack(new SlackMessage(app.getAppName(),
                    healthStatus, app.getCreatedAt(), app.getHealth().getDetails()));
            log.info(slackNotified.getBody());
            ResponseEntity<String> mailNotified = email(new EmailMessage(app.getAppName(),
                    healthStatus, app.getCreatedAt(), app.getHealth().getDetails()));
            log.info(mailNotified.getBody());
        } else {
            log.info("No notification for " + app.getAppName() + " since health is " + AppConstants.up);
        }
    }

    @Value("${slack.url}")
    private String slackUrl;

    @Value("${slack.disabled:true}")
    private Boolean slackDisabled;

    public ResponseEntity<String> sendSlack(SlackMessage slackMessage) {
        log.info("Came inside sendSlack for " + slackMessage.toString());
        try {
            log.debug("slackUrl = " + slackUrl);
            if (slackDisabled) {
                return new ResponseEntity<>(AppConstants.slack_diabled, HttpStatus.OK);
            } else if (!StringUtils.isEmpty(slackUrl)) {
                RestTemplate rest = new RestTemplate();
                RichMessage richMessage = new RichMessage(slackMessage.toJson());
                rest.postForEntity(slackUrl, richMessage.encodedMessage(), String.class);
                return new ResponseEntity<>(AppConstants.slack_sent, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(AppConstants.slack_url_empty, HttpStatus.OK);
            }

        } catch (Exception e) {
            return new ResponseEntity<>(AppConstants.slack_not_sent, HttpStatus.OK);
        }
    }

    @Value("${mail.disabled:true}")
    private Boolean mailDisabled;

    @Value("${mail.receiver}")
    private String receiver;

    @Value("${mail.sender}")
    private String sender;

    public ResponseEntity<String> email(EmailMessage emailMessage) {
        log.info("Came inside email for " + emailMessage.toString());
        MimeMessage mail = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mail);

        try {
            if (mailDisabled) {
                return new ResponseEntity<>(AppConstants.mail_diabled, HttpStatus.OK);
            } else {
                helper.setTo(receiver);
                helper.setFrom(sender);
                helper.setReplyTo(sender);
                helper.setSubject(AppConstants.mail_subject);
                helper.setText(emailMessage.toJson());
                mailSender.send(mail);
                return new ResponseEntity<>(AppConstants.mail_sent, HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(AppConstants.mail_not_sent, HttpStatus.OK);
        }
    }
}


