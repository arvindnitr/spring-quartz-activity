package com.arvind.quartz.scheduler.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("classpath:application.properties")
public class QuartzExpressionConfig {

    @Value("${time.printing.job.cron.expr:0 * 2-23 ? * *}")
    private String timePrintingCronConfig;
}
