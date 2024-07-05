package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateMarriageDate;

@Slf4j
public class MarriageCertificateDetails implements CcdPageConfiguration {

    private static final String UK = "UK";

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
            data.getApplication().getMarriageDetails().setPlaceOfMarriage(UK);
        }

        log.info("Validating Marriage Date for Case Id: {}", details.getId());
        final List<String> validationErrors = validateMarriageDate(data, "MarriageDate");
        if (!validationErrors.isEmpty()) {
            State state = details.getState();
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(validationErrors)
                .state(state)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
