package com.arvind.quartz.scheduler.activity.service;

import com.arvind.quartz.scheduler.activity.State;
import com.arvind.quartz.scheduler.activity.dao.ActivityRepository;
import com.arvind.quartz.scheduler.activity.model.ActivityEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository repository;

    public List<ActivityEntity> getActivities() {
        return repository.findAll();
    }

    public ActivityEntity getActivityById(Integer activityId) {
        return repository.findById(activityId).orElse(null);
    }

    public int saveActivity(ActivityEntity activityEntity) {
        activityEntity = repository.save(activityEntity);
        return activityEntity.getId();
    }

    private boolean updateActivityState(Integer activityId, State state) {
        boolean updateFlag = false;
        ActivityEntity activityEntity = repository.findById(activityId).orElse(null);
        if(Objects.nonNull(activityEntity)){
            activityEntity.setState(state.name());
            repository.save(activityEntity);
            updateFlag = true;
        }
        return updateFlag;
    }

    public boolean setActivitySuccessful(Integer activityId) {
        return updateActivityState(activityId, State.DONE);
    }

    public boolean setActivityFailed(Integer activityId) {
        return updateActivityState(activityId, State.FAILED);
    }

    public boolean setActivityCancelled(Integer activityId) {
        return updateActivityState(activityId, State.CANCELLED);
    }
}

