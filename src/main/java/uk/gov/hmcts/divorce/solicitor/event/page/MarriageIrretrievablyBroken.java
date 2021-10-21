package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MarriageIrretrievablyBroken implements CcdPageConfiguration {
    @Override
    public void addTo(final PageBuilder<CaseData, UserRole, State> pageBuilder) {

        pageBuilder
            .page("MarriageIrretrievablyBroken", this::midEvent)
            .pageLabel("Has the marriage irretrievably broken down (it cannot be saved)?")
            .label(
                "marriageIrretrievablyBrokenPara-1",
                "The marriage must have irretrievably broken down for the applicant to get a divorce. "
                    + "This means it cannot be saved.")
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicant1ScreenHasMarriageBroken)
                .done()
            .label(
                "MarriageNotIrretrievablyBroken",
                "The marriage must have irretrievably broken down for the applicant to get a divorce. "
                    + "This is the law in England and Wales.",
                "applicant1ScreenHasMarriageBroken=\"No\""
            );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for MarriageIrretrievablyBroken");

        CaseData data = details.getData();
        List<String> errors = new ArrayList<>();
        Application application = data.getApplication();

        if (!application.getApplicant1ScreenHasMarriageBroken().toBoolean()) {
            errors.add("To continue, applicant 1 must believe and declare that their marriage has irrevocably broken");
        }

        if (application.getApplicant2ScreenHasMarriageBroken() != null && !application.getApplicant2ScreenHasMarriageBroken().toBoolean()) {
            errors.add("To continue, applicant 2 must believe and declare that their marriage has irrevocably broken");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(errors)
            .build();
    }
}
