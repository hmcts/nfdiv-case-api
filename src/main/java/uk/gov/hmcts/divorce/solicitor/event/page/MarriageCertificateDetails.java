package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;

public class MarriageCertificateDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("MarriageCertificateDetails", this::midEvent)
            .pageLabel("Details from the certificate")
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .mandatoryWithLabel(MarriageDetails::getDate,"Date of ${labelContentMarriageOrCivilPartnership} from the certificate")
                    .mandatoryWithLabel(MarriageDetails::getApplicant1Name,"${labelContentApplicantsOrApplicant1s} full name")
                    .mandatoryWithLabel(MarriageDetails::getApplicant2Name,"${labelContentRespondentsOrApplicant2s} full name")
                    .mandatoryWithLabel(MarriageDetails::getMarriedInUk,
                        "Did the ${labelContentMarriageOrCivilPartnership} take place in the UK?")
                    .mandatory(MarriageDetails::getPlaceOfMarriage, "marriageMarriedInUk=\"No\"")
                    .mandatory(MarriageDetails::getCountryOfMarriage, "marriageMarriedInUk=\"No\"")
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        final CaseData data = details.getData();
        if (data.getApplication().getMarriageDetails().getMarriedInUk().toBoolean()) {
            data.getApplication().getMarriageDetails().setPlaceOfMarriage("UK");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
