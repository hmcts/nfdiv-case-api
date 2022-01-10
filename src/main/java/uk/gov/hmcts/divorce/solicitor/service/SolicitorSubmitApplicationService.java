package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import javax.servlet.http.HttpServletRequest;

import static java.lang.Integer.parseInt;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenApplicant2Approve.APPLICANT_2_APPROVE;
import static uk.gov.hmcts.divorce.citizen.event.CitizenApplicant2RequestChanges.CITIZEN_APPLICANT_2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

@Service
@Slf4j
public class SolicitorSubmitApplicationService {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Async
    public void submitEventForApprovalOrRequestingChanges(CaseDetails<CaseData, State> details) {
        final Application application = details.getData().getApplication();

        String eventId;
        if (YES.equals(application.getApplicant2ConfirmApplicant1Information())) {
            eventId = CITIZEN_APPLICANT_2_REQUEST_CHANGES;
        } else {
            eventId = APPLICANT_2_APPROVE;
        }

        String solicitorAuthToken = httpServletRequest.getHeader(AUTHORIZATION);
        User solUser = idamService.retrieveUser(solicitorAuthToken);

        final String serviceAuthorization = authTokenGenerator.generate();

        log.info("Submitting event id {} for case id: {}", eventId, details.getId());

        ccdUpdateService.submitEvent(details, eventId, solUser, serviceAuthorization);
    }
}
