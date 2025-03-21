package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.util.Optional;

import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;

@Component
@Slf4j
public class SetServiceType implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> details) {

        final CaseData caseData = details.getData();
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        log.info("Setting service method for case id {}", details.getId());

        var isSole = caseData.getApplicationType().isSole();
        var applicant1NotRepresented = !applicant1.isRepresented();
        
        var applicant2Overseas = !applicant2.isRepresented() && applicant2.isBasedOverseas();
        var applicant2SolOverseas = applicant2.isRepresented() && YesOrNo.YES.equals(
            Optional.ofNullable(applicant2.getSolicitor())
                .map(Solicitor::getAddressOverseas)
                .orElse(null)
        );

        if (isSole && applicant1NotRepresented && (applicant2Overseas || applicant2SolOverseas)) {
            caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        } else if (caseData.getApplication().getServiceMethod() == null) {
            caseData.getApplication().setServiceMethod(COURT_SERVICE);
        }
        log.info("Service method set to {} for case id {}", caseData.getApplication().getServiceMethod(), details.getId());

        return details;
    }
}
