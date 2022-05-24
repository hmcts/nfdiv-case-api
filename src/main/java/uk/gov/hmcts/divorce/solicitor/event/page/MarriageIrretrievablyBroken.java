package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.CivilPartnershipBroken.CIVIL_PARTNERSHIP_BROKEN;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageBroken.MARRIAGE_BROKEN;

@Slf4j
@Component
public class MarriageIrretrievablyBroken implements CcdPageConfiguration {
    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("MarriageIrretrievablyBroken", this::midEvent)
            .pageLabel("Statement of irretrievable breakdown")
            .complex(CaseData::getApplication)
            .mandatory(Application::getApplicant1HasMarriageBroken, "divorceOrDissolution=\"divorce\"")
            .mandatory(Application::getApplicant1HasCivilPartnershipBroken, "divorceOrDissolution=\"dissolution\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        log.info("Mid-event callback triggered for MarriageIrretrievablyBroken");

        CaseData data = details.getData();
        List<String> errors = new ArrayList<>();
        Application application = data.getApplication();

        if (data.isDivorce()) {
            if (application.getApplicant1HasMarriageBroken() == null
                || !(application.getApplicant1HasMarriageBroken().contains(MARRIAGE_BROKEN))) {
                errors.add("To continue, applicant 1 must believe and declare that their marriage has irrevocably broken");
            }
        } else {
            if (application.getApplicant1HasCivilPartnershipBroken() == null
                || !(application.getApplicant1HasCivilPartnershipBroken().contains(CIVIL_PARTNERSHIP_BROKEN))) {
                errors.add("To continue, applicant 1 must believe and declare that their marriage has irrevocably broken");
            }
        }

        if (application.getApplicant2ScreenHasMarriageBroken() != null && !application.getApplicant2ScreenHasMarriageBroken().toBoolean()) {
            errors.add("To continue, applicant 2 must believe and declare that their marriage has irrevocably broken");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(errors)
            .build();
    }
}
