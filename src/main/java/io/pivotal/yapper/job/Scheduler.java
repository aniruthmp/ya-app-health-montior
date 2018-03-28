package io.pivotal.yapper.job;

import io.pivotal.yapper.repository.AppRepository;
import io.pivotal.yapper.service.HealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
@Slf4j
@Profile("!local")
public class Scheduler {

    final AppRepository appRepository;
    final HealthService healthService;

    @Value("${dbretain:7}")
    private Integer dbretain;

    public Scheduler(AppRepository appRepository, HealthService healthService) {
        this.appRepository = appRepository;
        this.healthService = healthService;
    }

    /**
     * Poll health every 5 mins
     */
    @Scheduled(cron = "${cron.poll-health}")
    public void pollHealth() {
        log.info("pollHealth job started @ " + new Date());

        try {
            healthService.serviceInstances();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        log.info("pollHealth job completed @ " + new Date());
    }

    /**
     * Clean old database records everyday at 6am
     */
    @Scheduled(cron = "${cron.clean-db}")
    public void cleanupDatabase() {
        log.info("cleanupDatabase job started @ " + new Date());

        try {
            //Calculate the expiry date
            Calendar cal = Calendar.getInstance();
            Date today = cal.getTime();
            cal.add(Calendar.DATE, -dbretain);
            Date expiryDate = cal.getTime();
            log.info("cleanupDatabase expiryDate: " + expiryDate);
            this.appRepository.deleteByCreatedAtBefore(expiryDate);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        log.info("cleanupDatabase job completed @ " + new Date());
    }

}
