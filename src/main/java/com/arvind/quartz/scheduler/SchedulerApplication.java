package com.arvind.quartz.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@Slf4j
@SpringBootApplication
public class SchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchedulerApplication.class, args);
	}

	@Autowired
	private Scheduler scheduler;

	@Autowired
	@Qualifier("timePrintingJobDetail")
	private JobDetail timePrintingJobDetail;

	@Autowired
	@Qualifier("timePrintingTrigger")
	private Trigger timePrintingTrigger;

	@PostConstruct
	public void init() {
		try {
			scheduler.scheduleJob(timePrintingJobDetail, timePrintingTrigger);
		} catch (SchedulerException e) {
			log.error("Exception:" + e);
		}
	}
}
