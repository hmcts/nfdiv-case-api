package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;

@Slf4j
public class SolHowDoYouWantToApplyForDivorce implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("howDoYouWantToApplyForDivorce", this::midEvent)
            .pageLabel("Is this a divorce or dissolution application?")
            .complex(CaseData::getLabelContent)
                .readonly(LabelContent::getApplicant2, NEVER_SHOW)
                .readonly(LabelContent::getApplicant2UC, NEVER_SHOW)
                .readonly(LabelContent::getTheApplicant2, NEVER_SHOW)
                .readonly(LabelContent::getTheApplicant2UC, NEVER_SHOW)
                .readonly(LabelContent::getUnionType, NEVER_SHOW)
                .readonly(LabelContent::getUnionTypeUC, NEVER_SHOW)
                .readonly(LabelContent::getDivorceOrCivilPartnership, NEVER_SHOW)
                .readonly(LabelContent::getDivorceOrCivilPartnershipApplication, NEVER_SHOW)
                .readonly(LabelContent::getFinaliseDivorceOrEndCivilPartnership, NEVER_SHOW)
                .readonly(LabelContent::getApplicantOrApplicant1, NEVER_SHOW)
                .readonly(LabelContent::getDivorceOrEndCivilPartnership, NEVER_SHOW)
                .readonly(LabelContent::getMarriageOrCivilPartnership, NEVER_SHOW)
                .readonly(LabelContent::getDivorceOrLegallyEnd, NEVER_SHOW)
                .readonly(LabelContent::getApplicantsOrApplicant1s, NEVER_SHOW)
                .readonly(LabelContent::getTheApplicantOrApplicant1, NEVER_SHOW)
                .readonly(LabelContent::getGotMarriedOrFormedCivilPartnership, NEVER_SHOW)
                .readonly(LabelContent::getTheApplicantsOrApplicant1s, NEVER_SHOW)
                .readonly(LabelContent::getRespondentsOrApplicant2s, NEVER_SHOW)
            .done()
            .mandatory(CaseData::getDivorceOrDissolution)
            .label("soleLabelDivorce", "### Sole applications<br>"
                    + "\nThe other party responds to the divorce application after it's issued</small>",
                "divorceOrDissolution = \"divorce\"")
            .label("soleLabelDissolution", "### Sole applications"
                    + "\nThe other party responds to the  application to end their civil partnership after it's issued",
                "divorceOrDissolution = \"dissolution\"")
            .label("jointLabel", "### Joint applications"
                    + "\nThe other party joins and reviews the application before it's submitted.",
                "divorceOrDissolution = \"divorce\" OR divorceOrDissolution = \"dissolution\"")
            .mandatory(CaseData::getApplicationType,
                "divorceOrDissolution = \"divorce\" OR divorceOrDissolution = \"dissolution\"");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for howDoYouWantToApplyForDivorce");

        final CaseData data = details.getData();
        data.getLabelContent().setApplicationType(data.getApplicationType());
        data.getLabelContent().setUnionType(data.getDivorceOrDissolution());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
