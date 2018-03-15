package io.pivotal.appmonitor.repository;

import io.pivotal.appmonitor.domain.App;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppRepository extends MongoRepository<App, String> {
    public List<App> findByAppNameOrderByCreatedAtDesc(String appName);
}
