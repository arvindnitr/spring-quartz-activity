package com.arvind.quartz.scheduler.web;

import com.arvind.quartz.scheduler.activity.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/scheduler")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping(value="/activity")
    public ResponseEntity<?> activities(){
        return ResponseEntity.ok().body(activityService.getActivities());
    }
}
