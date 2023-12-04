package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;

@Component
@Slf4j
public abstract class AbstractTaskEventSubmit implements Runnable {

    private static final String SUBMIT_EVENT_ERROR = "Submit event failed for case(id={}), continuing to next case";
    private static final String DESERIALIZATION_ERROR = "Deserialization failed for case(id={}), continuing to next case";

    @Autowired
    private CcdUpdateService ccdUpdateService;

    public abstract void run();

    public void submitEvent(Long caseId, String eventId, User systemUser, String serviceAuth) {
        try {
            ccdUpdateService.submitEvent(caseId, eventId, systemUser, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error(SUBMIT_EVENT_ERROR, caseId);
        } catch (final IllegalArgumentException e) {
            log.error(DESERIALIZATION_ERROR, caseId);
        }
    }
}
