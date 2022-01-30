package com.arvind.quartz.scheduler.activity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "SCHEDULED_ACTIVITY")
public class ActivityEntity {

    @Id
    @GeneratedValue (strategy= GenerationType.SEQUENCE, generator="activitySeqGen")
    @SequenceGenerator(name = "activitySeqGen", sequenceName = "SCHEDULED_ACTIVITY_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private int id;

    @Column(name = "OPERATION")
    private String operation;

    @Column(name = "RESOURCE_TYPE")
    private String resourceType;

    @Column(name = "SUMMARY")
    private String summary;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "PARTICIPANTS")
    private String participants;

    @Column(name = "STATE")
    private String state;

    @Column(name = "CREATED_AT")
    private Timestamp createdAt;
}
