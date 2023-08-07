package uk.gov.hmcts.divorce.common.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.SetGeneralReferralDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralReferral.CASEWORKER_GENERAL_REFERRAL;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;

@Service
@Slf4j
public class GeneralReferralService {

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private SetGeneralReferralDetails setGeneralReferralDetails;

    public void caseWorkerGeneralReferral(final CaseDetails<CaseData, State> details) {
        if (FinalOrderRequested.equals(details.getState()) && hasOrderLateExplanation(details)) {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuthorization = authTokenGenerator.generate();
            final String caseId = details.getId().toString();

            ccdUpdateService
                .submitEventWithRetry(caseId, CASEWORKER_GENERAL_REFERRAL, setGeneralReferralDetails, user, serviceAuthorization);
        }
    }

    private boolean hasOrderLateExplanation(final CaseDetails<CaseData, State> details) {
        final FinalOrder finalOrder = details.getData().getFinalOrder();
        return null != finalOrder && finalOrder.hasFinalOrderLateExplanation();
    }
}
