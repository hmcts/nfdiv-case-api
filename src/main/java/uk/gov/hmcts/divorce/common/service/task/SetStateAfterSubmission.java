package uk.gov.hmcts.divorce.common.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateTTL.SYSTEM_UPDATE_TTL;

@Component
@RequiredArgsConstructor
@Slf4j
public class SetStateAfterSubmission implements CaseTask {

    private final CcdUpdateService ccdUpdateService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final Application application = caseData.getApplication();
        final boolean isHWFApplicant1 = application.isHelpWithFeesApplication();
        final boolean isHWFApplicant2 = application.isHelpWithFeesApplicationApplicant2();
        final boolean isSoleApplication =  nonNull(caseData.getApplicationType())
            && caseData.getApplicationType().isSole();
        final boolean isApplicant1AwaitingDocuments = !isEmpty(application.getApplicant1CannotUploadSupportingDocument())
            || isSoleApplication && application.isPersonalServiceMethod();
        final boolean isApplicant2AwaitingDocuments = application.hasAwaitingApplicant2Documents();

        boolean applicantIsAwaitingDocuments = (isApplicant1AwaitingDocuments && !isHWFApplicant1)
                || (!isSoleApplication && isApplicant2AwaitingDocuments && !isHWFApplicant2);
        boolean applicantNeedsHelpWithFees = (isSoleApplication && isHWFApplicant1)
            || (!isSoleApplication && isHWFApplicant1 && isHWFApplicant2);

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        if (applicantNeedsHelpWithFees) {
            caseDetails.setState(AwaitingHWFDecision);
            ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_UPDATE_TTL, user, serviceAuthorization);
        } else if (applicantIsAwaitingDocuments) {
            caseDetails.setState(AwaitingDocuments);
            ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_UPDATE_TTL, user, serviceAuthorization);
        } else if (!application.hasBeenPaidFor()) {
            caseDetails.setState(AwaitingPayment);
        } else {
            caseDetails.setState(Submitted);
            ccdUpdateService.submitEvent(caseDetails.getId(), SYSTEM_UPDATE_TTL, user, serviceAuthorization);
        }

        log.info("State set to {}, CaseID {}", caseDetails.getState(), caseDetails.getId());

        if (caseData.isWelshApplication()) {
            caseData.getApplication().setWelshPreviousState(caseDetails.getState());
            caseDetails.setState(WelshTranslationReview);
            log.info("State set to WelshTranslationReview, WelshPreviousState set to {}, CaseID {}",
                caseData.getApplication().getWelshPreviousState(), caseDetails.getId());
        }

        return caseDetails;
    }
}
