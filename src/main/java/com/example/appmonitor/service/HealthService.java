package com.example.appmonitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Slf4j
public class HealthService {

    @Autowired
    private DiscoveryClient discoveryClient;

    public ResponseEntity<List<ServiceInstance>> serviceInstancesByApplicationName(
             String applicationName) {
        List<ServiceInstance> serviceInstances = null;
        try {
            applicationName = applicationName.toUpperCase();
            log.info("Getting Instance details for " + applicationName);
            serviceInstances = this.discoveryClient.getInstances(applicationName);
            if(CollectionUtils.isEmpty(serviceInstances)){
                log.warn("No Instance details for " + applicationName);
                return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
            }
        } catch (Exception ex){
            log.error("Error getting Instance details: " + ex.getMessage());
            return new ResponseEntity<>(HttpStatus.SERVICE_UNAVAILABLE);
        }
        return new ResponseEntity<>(serviceInstances, HttpStatus.OK);
    }
}

