package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;

public class CorrectRelationshipDetails implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder
            .page("correctPaperCaseDetails", this::midEvent)
            .pageLabel("Correct paper case")
            .label("Label-CorrectYourApplication", "### Your application details")
            .mandatoryWithLabel(CaseData::getSupplementaryCaseType,
                "Not Applicable, Judicial Separation or Separation?")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getApplicantOrApplicant1UC, NEVER_SHOW)
            .done()
            .mandatory(CaseData::getDivorceOrDissolution, "supplementaryCaseType=\"notApplicable\"")
            .mandatory(CaseData::getApplicationType)
            .complex(CaseData::getApplication)
                .mandatoryWithLabel(Application::getScreenHasMarriageCert,
                    "Does ${labelContentTheApplicantOrApplicant1} have the ${labelContentMarriageOrCivilPartnership} certificate?")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        final CaseData data = details.getData();

        data.getLabelContent().setApplicationType(data.getApplicationType());
        data.getLabelContent().setUnionType(data.getDivorceOrDissolution());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
