package com.arvind.quartz.scheduler.activity.dao;

import com.arvind.quartz.scheduler.activity.model.ActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<ActivityEntity, Integer> {
}
