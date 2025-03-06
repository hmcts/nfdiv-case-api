package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateApplicant2NameForAllowedCharacters;

public class SolAboutApplicant2 implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant2", this::midEvent)
            .pageLabel("About the other party")
            .complex(CaseData::getApplicant2)
                .mandatoryWithLabel(Applicant::getFirstName,
                    "${labelContentApplicant2UC}'s first name")
                .optional(Applicant::getMiddleName,
                    null,
                    null,
                    "${labelContentApplicant2UC}'s middle name",
                    "If they have a middle name then you must enter it to avoid amendments later."
                    )
                .mandatoryWithLabel(Applicant::getLastName,
                    "${labelContentApplicant2UC}'s last name")
                .mandatory(Applicant::getNameDifferentToMarriageCertificate,
                    null,
                    null,
                    "Is ${labelContentTheApplicant2}'s name different from the name on the "
                        + "${labelContentMarriageOrCivilPartnership} certificate?",
                    "")
                .mandatoryWithoutDefaultValue(
                    Applicant::getNameDifferentToMarriageCertificateMethod,
                    "applicant2NameDifferentToMarriageCertificate=\"Yes\"",
                    "What evidence will be provided for the name change?")
                .mandatoryWithoutDefaultValue(
                    Applicant::getNameDifferentToMarriageCertificateOtherDetails,
                    "applicant2NameDifferentToMarriageCertificateMethod=\"other\"",
                    "If not through marriage or deed poll, please provide details of how they legally changed they name")
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        CaseData caseData = details.getData();

        List<String> validationErrors = validateApplicant2NameForAllowedCharacters(caseData);
        if (!validationErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(validationErrors)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .errors(null)
            .build();
    }
}
