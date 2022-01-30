package com.arvind.quartz.scheduler.activity;

import com.arvind.quartz.scheduler.activity.service.ActivityService;
import com.arvind.quartz.scheduler.activity.model.ActivityEntity;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Slf4j
public class ActivityAspect {

    private static String SUBJECT = "subject";
    private static String PARTICIPANTS = "participants";
    public static String ACTIVITY_ID = "activityId";

    @Autowired
    private ActivityService activityService;

    /**
     * AOP to process sync activity
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.arvind.quartz.scheduler.activity.Activity)")
    public Object processActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        Activity activity = getActivity(joinPoint);
        Operation operation = activity.operation();
        int activityId = persistActivityInProgressMsg(joinPoint, activity, operation);
        try {
            Object result = joinPoint.proceed();
            activityService.setActivitySuccessful(activityId);
            log.info("Activity subject:" + activity.subject().name()+ ", Operation: "+ activity.operation()+ ", " +State.DONE.name());
            return result;
        } catch (InterruptedException e) {
            activityService.setActivityCancelled(activityId);
            log.info("Activity subject:" + activity.subject().name()+ ", Operation: "+ activity.operation()+ ", "+ State.CANCELLED.name());
            throw e;
        } catch (Throwable t) {
            activityService.setActivityFailed(activityId);
            log.info("Activity subject:" + activity.subject().name()+ ", Operation: "+ activity.operation()+ ", "+State.FAILED.name());
            throw t;
        }
    }

    /**
     * This method persists initial activity msg in DB and sets activity_id in threadLocal(MDC)
     *
     * @param joinPoint
     * @param activity
     * @param operation
     */
    private int persistActivityInProgressMsg(ProceedingJoinPoint joinPoint, Activity activity, Operation operation) {
        String activitySummaryInitialMsg = operation.getActivitySummary();
        Map<String, String> activityContextValues = getParameterAnnotations(joinPoint);
        LocalDateTime activityTimeStamp = LocalDateTime.now();
        String activitySummary = constructActivitySummaryMsg(activitySummaryInitialMsg, activityContextValues, "System", activityTimeStamp);
        int activityId = persistActivity(activity, activitySummary, activityContextValues, State.IN_PROGRESS, activityTimeStamp);
        MDC.put(ACTIVITY_ID, String.valueOf(activityId));
        return activityId;
    }

    /**
     * Get Activity annotation from joinpoint
     *
     * @param joinPoint
     * @return
     */
    private Activity getActivity(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Activity activity = method.getAnnotation(Activity.class);
        Operation operation = activity.operation();
        return activity;
    }

    /**
     * Persist activity  and activityMsg in DB
     *
     * @param activity
     * @param activitySummary
     * @return
     */
    private int persistActivity(Activity activity,
                                String activitySummary,
                                Map<String, String> activityContextValues,
                                State state,
                                LocalDateTime activityTimeStamp) {
        String subject = null;
        String participants = null;
        if (activityContextValues != null) {
            subject = activityContextValues.get(SUBJECT);
            participants = activityContextValues.get(PARTICIPANTS);
        }
        log.info("ActivitySummary Msg:" + activitySummary);
        log.info("Subject: {}, Participants: {}, Operation State: {}", subject, participants, state);
        ActivityEntity activityEntity = ActivityEntity.builder().resourceType(activity.subject().name()).subject(subject).participants(participants).state(state.name()).
                operation(activity.operation().getOp()).summary(activitySummary).createdAt(Timestamp.valueOf(activityTimeStamp)).build();
        return activityService.saveActivity(activityEntity);
    }

    /**
     * Construct activity summary msg by replacing placeholders
     *
     * @param activitySummaryMsg
     * @param activityContextValues
     * @param loggedInUser
     * @return
     */
    private String constructActivitySummaryMsg(String activitySummaryMsg,
                                               Map<String, String> activityContextValues,
                                               String loggedInUser,
                                               LocalDateTime activityTimeStamp) {
        activitySummaryMsg = activitySummaryMsg.replace("{user}", "[" + loggedInUser + "]");
        activitySummaryMsg = activitySummaryMsg.replace("{time}", activityTimeStamp.toString());

        if (activityContextValues == null) {
            log.warn("Could not replace placeholders in the initial message.");
            return activitySummaryMsg;
        }

        if (activityContextValues.containsKey(SUBJECT)) {
            String subjectVal = activityContextValues.get(SUBJECT);
            if (subjectVal != null && !subjectVal.isEmpty()) {
                activitySummaryMsg = activitySummaryMsg.replace("{subject}", "[" + subjectVal + "]");
            }
        }
        if (activityContextValues.containsKey(PARTICIPANTS)) {
            String participantsVal = activityContextValues.get(PARTICIPANTS);
            if (participantsVal != null && !participantsVal.isEmpty()) {
                activitySummaryMsg = activitySummaryMsg.replace("{participants}", "[" + participantsVal + "]");
            }
        }
        return activitySummaryMsg;
    }

    /**
     * One of the argument has activityContext annotation, return subject and parameter values from that argument
     *
     * @param joinPoint
     * @return
     */
    private Map<String, String> getParameterAnnotations(JoinPoint joinPoint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
        Annotation[][] annotations;
        try {
            annotations = joinPoint.getTarget().getClass().
                    getMethod(methodName, parameterTypes).getParameterAnnotations();
        } catch (Exception e) {
            log.error("Parameter annotations will not be processed: " + e);
            return null;
        }

        int count = 0;
        //Process annotation of each argument, return when activityContext annotation is found
        for (Object arg : joinPoint.getArgs()) {
            for (Annotation annotation : annotations[count]) {
                if (annotation.annotationType() == ActivityContext.class) {
                    ActivityContext activityContext = (ActivityContext) annotation;
                    log.debug(annotation + " -> " + arg);
                    // Only one argument will have activityContext annotation
                    return getSubjectAndParticipantsFromActivityContext(arg, activityContext);
                }
            }
            count++;
        }
        return null;
    }

    /**
     * This method processes activityContext and returns subject and participants
     *
     * @param arg
     * @param activityContext
     */
    private Map<String, String> getSubjectAndParticipantsFromActivityContext(Object arg, ActivityContext activityContext) {

        Map<String, String> activityContextValues = new HashMap<>();
        String subject = activityContext.subject();
        String participants = activityContext.participants();
        log.debug("Subject:" + subject + ", participants:" + participants);
        try {
            String subjectValue = getFieldValue(arg, subject);
            activityContextValues.put(SUBJECT, subjectValue);
            log.debug("Subject - (" + subject + " : " + subjectValue + ")");

            if (participants.isEmpty()) {
                return activityContextValues;
            }
            String participantArr[] = participants.split(",");
            StringBuilder commaSeparatedParticipantValues = new StringBuilder();
            for (String participant : participantArr) {
                String participantValue = getFieldValue(arg, participant);
                log.debug("Participant - " + participant + " : " + participantValue);
                if (participantValue != null) {
                    commaSeparatedParticipantValues = commaSeparatedParticipantValues.append(participantValue + ",");
                }
            }
            String participantValues = null;
            if (commaSeparatedParticipantValues.length() > 0 && commaSeparatedParticipantValues.toString().contains(",")) {
                participantValues = commaSeparatedParticipantValues.substring(0, commaSeparatedParticipantValues.lastIndexOf(","));
            }
            log.info("participants: " + participantValues);
            activityContextValues.put(PARTICIPANTS, participantValues);
        } catch (Exception e) {
            log.error("Exception: " + e);
        }
        return activityContextValues;
    }

    /**
     * Get field from class using reflection
     *
     * @param arg
     * @param fieldName
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     */
    private String getFieldValue(Object arg, String fieldName) throws ClassNotFoundException, IllegalAccessException {

        Field field;
        String className = null;
        try {
            className = arg.getClass().getName();
            field = Class.forName(arg.getClass().getName()).getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            log.error("Field " + fieldName + "not found in Class " + className);
            return null;
        }
        field.setAccessible(true);
        return field != null ? field.get(arg).toString() : null;
    }
}

