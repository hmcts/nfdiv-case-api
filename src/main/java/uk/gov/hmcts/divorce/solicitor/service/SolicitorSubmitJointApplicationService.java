package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.Applicant2Approve.APPLICANT_2_APPROVE;
import static uk.gov.hmcts.divorce.common.event.Applicant2RequestChanges.APPLICANT_2_REQUEST_CHANGES;

@Service
@Slf4j
public class SolicitorSubmitJointApplicationService {

    private static final Long DELAY = 2L;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Async
    public void submitEventForApprovalOrRequestingChanges(final CaseDetails<CaseData, State> details) throws InterruptedException {
        final Application application = details.getData().getApplication();

        String eventId = YES.equals(application.getApplicant2ConfirmApplicant1Information())
            ? APPLICANT_2_REQUEST_CHANGES
            : APPLICANT_2_APPROVE;

        User solUser = idamService.retrieveSystemUpdateUserDetails();

        final String serviceAuthorization = authTokenGenerator.generate();

        log.info("Submitting event id {} for case id: {}", eventId, details.getId());

        TimeUnit.SECONDS.sleep(DELAY);
        ccdUpdateService.submitEvent(details, eventId, solUser, serviceAuthorization);
    }
}
