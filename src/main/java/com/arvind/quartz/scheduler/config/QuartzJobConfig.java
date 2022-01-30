package com.arvind.quartz.scheduler.config;

import com.arvind.quartz.scheduler.activity.ActivityAspect;
import com.arvind.quartz.scheduler.quartz.job.TimePrintingJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.sql.Date;
import java.text.ParseException;
import java.time.ZonedDateTime;

@Configuration
@EnableAspectJAutoProxy
public class QuartzJobConfig {

    @Autowired
    private QuartzExpressionConfig quartzExpressionConfig;

    @Bean
    public ActivityAspect activityAspect() {
        return new ActivityAspect();
    }

    @Bean("timePrintingJobDetail")
    public JobDetail timePrintingJobDetail() {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("time", System.currentTimeMillis());

        return JobBuilder.newJob(TimePrintingJob.class).withIdentity("TimePrintingJob","TEST").
                withDescription("Send time-printing job").setJobData(jobDataMap).storeDurably().build();
    }

    @Bean("timePrintingTrigger")
    public Trigger timePrintingTriggerDetail() {
        JobDetail timePrintingJobDetail = timePrintingJobDetail();
        String cronExpression = quartzExpressionConfig.getTimePrintingCronConfig();
        ScheduleBuilder scheduler;
        try {
            CronExpression.validateExpression(cronExpression);
            scheduler= CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionFireAndProceed();

        } catch (ParseException e) {
            scheduler = SimpleScheduleBuilder.repeatHourlyForever();
        }
        return TriggerBuilder.newTrigger().withIdentity(timePrintingJobDetail.getKey().getName(), "time-printing-triggers").
                withDescription("Send time-printing triggers").startAt(Date.from(ZonedDateTime.now().toInstant())).
                withSchedule(scheduler).forJob(timePrintingJobDetail.getKey()).
                build();
    }

}
