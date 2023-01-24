package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

@Component
@Slf4j
public abstract class AbstractTaskEventSubmit implements Runnable {

    private static final String SUBMIT_EVENT_ERROR = "Submit event failed for case(id={}), continuing to next case";
    private static final String DESERIALIZATION_ERROR = "Deserialization failed for case(id={}), continuing to next case";

    @Autowired
    private CcdUpdateService ccdUpdateService;

    public abstract void run();

    public void submitEvent(CaseDetails caseDetails, String eventId, User systemUser, String serviceAuth) {
        try {
            ccdUpdateService.submitEvent(caseDetails, eventId, systemUser, serviceAuth);
        } catch (final CcdManagementException e) {
            log.error(SUBMIT_EVENT_ERROR, caseDetails.getId());
        } catch (final IllegalArgumentException e) {
            log.error(DESERIALIZATION_ERROR, caseDetails.getId());
        }
    }
}
