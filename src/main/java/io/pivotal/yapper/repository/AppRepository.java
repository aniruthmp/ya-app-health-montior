package io.pivotal.yapper.repository;

import io.pivotal.yapper.domain.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface AppRepository extends JpaRepository<App, Long> {
    public List<App> findByAppNameOrderByCreatedAtDesc(String appName);

    /**
     * This methods deletes all the records whose 'createdAt' date is less than 'expiryDate'
     */
    @Modifying
    @Transactional
    public void deleteByCreatedAtBefore(Date expiryDate);
}
