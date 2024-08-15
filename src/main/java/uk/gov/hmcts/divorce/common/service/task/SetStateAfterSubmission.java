package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;

@Component
@Slf4j
public class SetStateAfterSubmission implements CaseTask {

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

        if (applicantNeedsHelpWithFees) {
            caseDetails.setState(AwaitingHWFDecision);
        } else if (applicantIsAwaitingDocuments) {
            caseDetails.setState(AwaitingDocuments);
        } else if (!application.hasBeenPaidFor()) {
            caseDetails.setState(AwaitingPayment);
        } else {
            caseDetails.setState(Submitted);
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
