package com.arvind.quartz.scheduler.web;

import com.arvind.quartz.scheduler.dto.*;
import com.arvind.quartz.scheduler.service.PodDbPasswordRotationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/scheduler")
public class JobSchedulerController {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private PodDbPasswordRotationService podDbPasswordRotationService;

    @GetMapping(value="/health")
    public ResponseEntity<String> health(){
        return ResponseEntity.ok().body("Success");
    }

    @PatchMapping(value="/pause/{groupName}/{jobName}")
    public ResponseEntity<String> pause(@PathVariable String jobName, @PathVariable
            String groupName) {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not pause job "+ jobName +
                    "of group "+ groupName + ", Please try after some time ...");
        }
        return ResponseEntity.ok().body("Success");
    }

    @PatchMapping(value="/interrupt/{groupName}/{jobName}")
    public ResponseEntity<String> interrupt(@PathVariable String jobName, @PathVariable
            String groupName) {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        try {
            scheduler.interrupt(jobKey);
        } catch (SchedulerException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not interrupt job "+ jobName +
                    "of group "+ groupName + ", Please try after some time ...");
        }
        return ResponseEntity.ok().body("Success");
    }

    @PatchMapping(value="/resume/{groupName}/{jobName}")
    public ResponseEntity<String> resume(@PathVariable String jobName, @PathVariable String groupName){
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not resume job "+ jobName +
                    "of group "+ groupName + ", Please try after some time ...");
        }
        return ResponseEntity.ok().body("Success");
    }

    @PatchMapping(value="/pauseAll")
    public ResponseEntity<String> pauseAll() {
        try {
            scheduler.pauseAll();
        } catch (SchedulerException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not pause jobs, Please try after some time ...");
        }
        return ResponseEntity.ok().body("Success");
    }

    @PatchMapping(value="/resumeAll")
    public ResponseEntity<String> resumeAll(@RequestParam(required = false) String groupName){
        try {
            if(Objects.nonNull(groupName)) {

            }
            else{
                scheduler.resumeAll();
            }
        } catch (SchedulerException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not resume jobs, Please try after some time ...");
        }
        return ResponseEntity.ok().body("Success");
    }

    @PatchMapping(value="/pauseAll/{groupName}")
    public ResponseEntity<String> pauseGroupJobs(@PathVariable String groupName){
        List<String> pauseFailedForJobs = new ArrayList<>();
        List<String> pauseSuccessfulForJobs = new ArrayList<>();
        try {
            Set<JobKey> jobKeySet = scheduler.getJobKeys(GroupMatcher.groupEquals(groupName));
            for(JobKey jobKey: jobKeySet) {
                try {
                    scheduler.resumeJob(jobKey);
                    pauseSuccessfulForJobs.add(jobKey.getName());
                } catch(SchedulerException e) {
                    log.error("Could not patch job {} of group {}, Exception: {}", jobKey.getName(), jobKey.getGroup(), e );
                    pauseFailedForJobs.add(jobKey.getName());
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not pause jobs, Please try after some time ...");
        }
        if(pauseFailedForJobs.size()>0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not pause jobs - "+ pauseFailedForJobs + " for group " + groupName);
        }
        return ResponseEntity.ok().body("Successfully paused jobs - "+ pauseSuccessfulForJobs + " for group " + groupName);
    }

    @PatchMapping(value="/resumeAll/{groupName}")
    public ResponseEntity<String> resumeGroupJobs(@PathVariable String groupName){
        List<String> resumeFailedForJobs = new ArrayList<>();
        List<String> resumeSuccessfulForJobs = new ArrayList<>();
        try {
            Set<JobKey> jobKeySet = scheduler.getJobKeys(GroupMatcher.groupEquals(groupName));
            for(JobKey jobKey: jobKeySet) {
                try {
                    scheduler.resumeJob(jobKey);
                    resumeSuccessfulForJobs.add(jobKey.getName());
                } catch(SchedulerException e) {
                    log.error("Could not resume job {} of group {}, Exception: {}", jobKey.getName(), jobKey.getGroup(), e );
                    resumeFailedForJobs.add(jobKey.getName());
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not resume jobs, Please try after some time ...");
        }
        if(resumeFailedForJobs.size()>0) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not resume jobs - "+ resumeFailedForJobs + " for group " + groupName);
        }
        return ResponseEntity.ok().body("Successfully resumed jobs - "+ resumeSuccessfulForJobs + " for group " + groupName);
    }

    @GetMapping(value="/jobs")
    public ResponseEntity<ScheduleDetails> jobs(){
        List<ScheduledJobDetails> jobDetails = new ArrayList<>();
        ScheduleDetails scheduleDetails = null;
        try {
            List<String> groupNames= scheduler.getJobGroupNames();
            for(String groupName: groupNames) {
                Set<JobKey> jobKeySet = scheduler.getJobKeys(GroupMatcher.groupEquals(groupName));
                for(JobKey jobKey: jobKeySet) {
                    List<String> triggerTimeDetails = scheduler.getTriggersOfJob(jobKey).stream().
                            map(trigger -> trigger.getKey().getName() + ": " +trigger.getStartTime()+"-->"+trigger.getPreviousFireTime()+"-->"+
                            trigger.getNextFireTime()+"-->"+trigger.getEndTime()).collect(Collectors.toList());
                    jobDetails.add(ScheduledJobDetails.builder().groupName(groupName).
                            jobName(jobKey.getName()).triggerTimeDetails(triggerTimeDetails).build());
                }
            }

            List<String> currentlyExecutingJobs = scheduler.getCurrentlyExecutingJobs().stream().map(cej -> cej.getJobDetail().getKey().getName()).collect(Collectors.toList());
            Set<String> pausedTriggerGroups = scheduler.getPausedTriggerGroups();
            scheduleDetails = ScheduleDetails.builder().name(scheduler.getSchedulerName()).instanceId(scheduler.getSchedulerInstanceId()).
                    scheduledJobDetails(jobDetails).
                    pausedTriggerGroups(pausedTriggerGroups).currentlyExecutingJobs(currentlyExecutingJobs).
                    numberOfJobs(scheduler.getMetaData().getNumberOfJobsExecuted()).
                    runningSince(scheduler.getMetaData().getRunningSince()).
                    build();
        } catch (Exception e){
            log.info("Could not fetch job details" + e);
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not fetch job details, Please try after some time ...");
        }
        return ResponseEntity.ok().body(scheduleDetails);
    }

    @PostMapping(value="/podDbPasswordRotation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StandardResponse> schedulePodDbPasswordRotation(@Valid @RequestBody PodDbDetails podDbDetails) {
        ZonedDateTime dateTime = ZonedDateTime.of(podDbDetails.getDateTime(), podDbDetails.getTimeZone());
        if(dateTime.isBefore(ZonedDateTime.now())) {
            StandardResponse response = new StandardResponse(false, "datetime must be after current time...");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        StandardResponse response = podDbPasswordRotationService.schedulePodDbPasswordRotation(podDbDetails);
        if(response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @PostMapping(value="/reschedule/{groupName}/{jobName}")
    public ResponseEntity<String> rescheduleJob(@PathVariable String groupName, @PathVariable String jobName,
                                                @RequestBody CronTriggerDetails cronTrigger) {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        try {
            List<? extends Trigger> triggers =  scheduler.getTriggersOfJob(jobKey);
            for(Trigger trigger: triggers) {
                if(trigger instanceof  CronTrigger) {
                    CronTriggerImpl ct = (CronTriggerImpl) trigger;
                    if(Objects.nonNull(cronTrigger.getCronExpression()) && CronExpression.isValidExpression(cronTrigger.getCronExpression())) {
                        ct.setCronExpression(cronTrigger.getCronExpression());
                    }
                    if(Objects.nonNull(cronTrigger.getStartTime()))
                        ct.setStartTime(cronTrigger.getStartTime());
                    if(Objects.nonNull(cronTrigger.getEndTime()))
                        ct.setEndTime(cronTrigger.getEndTime());
                    if(Objects.nonNull(cronTrigger.getTimeZone()))
                        ct.setTimeZone(TimeZone.getTimeZone(cronTrigger.getTimeZone()));
                    scheduler.rescheduleJob(trigger.getKey(), trigger);
                }
            }
        } catch (ParseException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not reschedule jobs, invalid cron expression -" + cronTrigger.getCronExpression());
        } catch (SchedulerException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not pause jobs, Please try after some time ...");
        }
        return ResponseEntity.ok().body("Success");
    }

    @PatchMapping(value="/executeNow/{groupName}/{jobName}")
    public ResponseEntity<String> executeNow(@PathVariable String groupName, @PathVariable String jobName) {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        try {
            SimpleTrigger trigger = TriggerBuilder.newTrigger().withIdentity(UUID.randomUUID().toString(), "executeNowGroup").
                    withDescription("Execute now trigger for job" + jobName).forJob(jobKey).startNow().
                    withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow()).
                    build();
            scheduler.scheduleJob(trigger);
        } catch (SchedulerException e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not add trigger for job, Please try after some time ...");
        }
        return ResponseEntity.ok().body("Success");
    }

}
