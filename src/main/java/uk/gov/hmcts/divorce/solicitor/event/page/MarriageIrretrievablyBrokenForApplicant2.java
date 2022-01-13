package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;

@Slf4j
@Component
public class MarriageIrretrievablyBrokenForApplicant2 implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "applicant2ScreenHasMarriageBroken=\"ALWAYS_HIDE\"";

    @Autowired
    private MarriageIrretrievablyBroken marriageIrretrievablyBroken;

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("MarriageIrretrievablyBroken", this::midEvent)
            .pageLabel("Statement of irretrievable breakdown")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getMarriageOrCivilPartnership, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getDivorceOrLegallyEnd, ALWAYS_HIDE)
                .done()
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicant2ScreenHasMarriageBroken)
                .done()
            .label(
                "MarriageNotIrretrievablyBroken",
                "The ${labelContentMarriageOrCivilPartnership} must have broken down irretrievably "
                    + "for the applicant 2 to ${labelContentDivorceOrLegallyEnd}.",
                "applicant2ScreenHasMarriageBroken=\"No\""
            );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                   final CaseDetails<CaseData, State> detailsBefore) {
        return marriageIrretrievablyBroken.midEvent(details, detailsBefore);
    }
}
