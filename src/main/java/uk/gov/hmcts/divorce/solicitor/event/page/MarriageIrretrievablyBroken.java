package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

@Slf4j
public class MarriageIrretrievablyBroken implements CcdPageConfiguration {
    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("MarriageIrretrievablyBroken", this::midEvent)
            .pageLabel("Has the marriage irretrievably broken down (it cannot be saved)?")
            .label(
                "LabelNFDBanner-MarriageIrretrievablyBroken",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-MarriageIrretrievablyBroken",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .label(
                "marriageIrretrievablyBrokenPara-1",
                "The marriage must have irretrievably broken down for the applicant to get a divorce. "
                    + "This means it cannot be saved.")
            .mandatory(CaseData::getApplicant1ScreenHasMarriageBroken)
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

        if (!data.getApplicant1ScreenHasMarriageBroken().toBoolean()) {
            errors.add("To continue, applicant 1 must believe and declare that their marriage has irrevocably broken");
        }

        if (data.getApplicant2ScreenHasMarriageBroken() != null && !data.getApplicant2ScreenHasMarriageBroken().toBoolean()) {
            errors.add("To continue, applicant 2 must believe and declare that their marriage has irrevocably broken");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(errors)
            .build();
    }
}
