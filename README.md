
[![GitHub license](https://img.shields.io/crates/l/rustc-serialize.svg)](https://innersource.accenture.com/projects/MICROSERVICES/repos/spring-cloud-microservices/browse/LICENSE)  
  
# ya-app-health-montior  
Yet Another Health monitor for App(s) deployed in PCF and using SCS Eureka Service  
  
  
The purpose of this app is to poll for health information for a given list of microservices deployed in PCF and registered to Eureka Service Registry. Specifically this app uses the SCS tile. Obviously this can be changed to Consul or any other Service Discovery.  
At the moment, [MongoDB](https://www.mongodb.com/) is used to persist the polled information.  

Method	| Path	| Description
------------- | ------------------------- | -------------
GET	| /health	| Actuator health for this application :)
GET	| /ondemand/health/{applicationName}	| Get current health for a given application	
GET	| /ondemand/health	| Get current health for all the apps configured in the property file
GET	| /latest/health/{applicationName}| Get the latest known health from the database for a given application	
GET	| /latest/health	| Get the latest known health from the database for all the apps configured in the property file
GET	| /ping	| Similar to `/latest/health`, but gives very basic health information

#### Test URLs
https://ya-app-health-monitor.cfapps.io/ondemand/health/account-service

![Capture](https://github.com/aniruthmp/ya-app-health-montior/blob/master/docs/ondemand-account.png)

https://ya-app-health-monitor.cfapps.io/ondemand/health

![Capture](https://github.com/aniruthmp/ya-app-health-montior/blob/master/docs/ondemand.png)

https://ya-app-health-monitor.cfapps.io/latest/health/customer-service

![Capture](https://github.com/aniruthmp/ya-app-health-montior/blob/master/docs/latest-cust.png)

https://ya-app-health-monitor.cfapps.io/latest/health

![Capture](https://github.com/aniruthmp/ya-app-health-montior/blob/master/docs/latest.png)

https://ya-app-health-monitor.cfapps.io/ping

![Capture](https://github.com/aniruthmp/ya-app-health-montior/blob/master/docs/ping.png)