package uk.gov.hmcts.divorce.systemupdate.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static java.lang.Character.toLowerCase;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledTaskRunner {

    private final ApplicationContext context;

    public void run(String taskName) {
        final var beanName = toLowerCase(taskName.charAt(0)) + taskName.substring(1);
        final var task = getTask(beanName);

        if (task != null) {
            log.info("Running task: {}", beanName);
            task.run();
        } else {
            log.error("Task not found: {}", beanName);
        }
    }

    @Nullable
    private Runnable getTask(String beanName) {
        try {
            return (Runnable) context.getBean(beanName);
        } catch (Exception e) {
            log.error("Error finding task", e);
            return null;
        }
    }

}
