package uk.gov.hmcts.divorce.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.systemupdate.schedule.SystemRemindApplicant2Task;

@Service
@Slf4j
public class ScheduledTaskRunner {

    @Autowired
    private SystemRemindApplicant2Task systemRemindApplicant2Task;

    public void run(String taskName) {
        final var task = getTask(taskName);

        if (task != null) {
            log.info("EXECUTING : {}", taskName);
            task.run();
        } else {
            log.error("Task not found: {}", taskName);
        }
    }


    private Runnable getTask(final String taskName) {
        switch (taskName) {
            case "Applicant2Reminder":
                return systemRemindApplicant2Task;
            default:
                return null;
        }
    }
}
