package uk.gov.hmcts.divorce.solicitor.event.page;

import org.apache.commons.validator.routines.EmailValidator;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

public class SolAboutApplicant1 implements CcdPageConfiguration {

    private static final String INVALID_EMAIL_ERROR = "You have entered an invalid email address. "
        + "Please check the email and enter it again, before submitting the application.";

    private static final String DARK_HORIZONTAL_RULE =
        "![Dark Rule](https://raw.githubusercontent.com/hmcts/nfdiv-case-api/master/resources/image/LabelDarkHorizontalRule.png)";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant1", this::midEvent)
            .pageLabel("About the applicant")
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getFirstName,
                    "${labelContentApplicantsOrApplicant1s} first name")
                .optionalWithLabel(Applicant::getMiddleName,
                    "${labelContentApplicantsOrApplicant1s} middle name")
                .mandatoryWithLabel(Applicant::getLastName,
                    "${labelContentApplicantsOrApplicant1s} last name")
                .mandatoryWithLabel(Applicant::getNameDifferentToMarriageCertificate,
                    "Is ${labelContentTheApplicantOrApplicant1} name different to that on the"
                        + " ${labelContentMarriageOrCivilPartnership} certificate?")
                .mandatoryWithoutDefaultValue(Applicant::getNameDifferentToMarriageCertificateMethod,
                    "applicant1NameDifferentToMarriageCertificate=\"Yes\"",
                    "What evidence will be provided for the name change? ")
                .mandatoryWithoutDefaultValue(Applicant::getNameDifferentToMarriageCertificateOtherDetails,
                    "applicant1NameDifferentToMarriageCertificateMethod=\"other\"",
                    "If not through marriage or deed poll, please provide details of how they legally changed they name")
                .mandatoryWithoutDefaultValue(Applicant::getGender, "divorceOrDissolution=\"dissolution\"",
                "Is ${labelContentTheApplicantOrApplicant1} male or female?")
                .done()
            .complex(CaseData::getApplication)
                .mandatory(Application::getDivorceWho, "divorceOrDissolution=\"divorce\"", null,
                "Who is ${labelContentTheApplicantOrApplicant1} divorcing?")
                .complex(Application::getMarriageDetails)
                    .mandatory(MarriageDetails::getFormationType)
                    .done()
                .done()
            .label("contactDetails", "# ${labelContentApplicantsOrApplicant1s} contact details")
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getEmail,
                    "${labelContentApplicantsOrApplicant1s} email address")
                .optionalWithLabel(Applicant::getPhoneNumber,
                    "${labelContentApplicantsOrApplicant1s} phone number")
                .optionalWithLabel(Applicant::getAddressOverseas, "Is ${labelContentApplicantsOrApplicant1s} address international?")
                .mandatoryWithLabel(Applicant::getAddress,
                    "${labelContentApplicantsOrApplicant1s} home address")
                .label("LabelHorizontalLine1-SolAboutApplicant1", DARK_HORIZONTAL_RULE)
                .mandatory(Applicant::getContactDetailsType)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        CaseData caseData = details.getData();
        Applicant applicant1 = caseData.getApplicant1();

        boolean validEmail = EmailValidator.getInstance().isValid(applicant1.getEmail());
        if (!validEmail) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(INVALID_EMAIL_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .errors(null)
            .build();
    }
}
