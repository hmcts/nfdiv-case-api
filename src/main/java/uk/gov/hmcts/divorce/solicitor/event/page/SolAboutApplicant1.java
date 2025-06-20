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

import static uk.gov.hmcts.divorce.caseworker.event.page.UpdateContactDetails.APPLICANT_REFUGE_LABEL;
import static uk.gov.hmcts.divorce.caseworker.event.page.UpdateContactDetails.THE_APPLICANT_OR_APPLICANT1;
import static uk.gov.hmcts.divorce.common.ccd.PageBuilder.andShowCondition;

public class SolAboutApplicant1 implements CcdPageConfiguration {

    private static final String INVALID_EMAIL_ERROR = "You have entered an invalid email address. "
        + "Please check the email and enter it again, before submitting the application.";

    private static final String DARK_HORIZONTAL_RULE =
        "![Dark Rule](https://raw.githubusercontent.com/hmcts/nfdiv-case-api/master/resources/image/LabelDarkHorizontalRule.png)";

    public static final String FIRST_NAME_LABEL = """
       %s first name
        """;
    public static final String FIRST_NAME_HINT = """
        Do not enter an abbreviated name or a nickname unless it is their legal name.
        For example, if David is their legal name, do not enter Dave instead of David
        """;

    public static final String IS_NAME_DIFFERENT_LABEL = """
        Is the %s name different from the name on the ${labelContentMarriageOrCivilPartnership} certificate?
        """;
    public static final String IS_NAME_DIFFERENT_HINT = """
        If the %s name is different on the ${labelContentMarriageOrCivilPartnership} certificate, you will have to upload some
        evidence like a government issued ID, a passport, driving licence or birth certificate, deed poll.
        """;
    private static final String APPLICANTS_OR_APPLICANT1S = "${labelContentApplicantsOrApplicant1s}";

    public static final String HOW_NAME_WRITTEN_ON_CERTIFICATE_LABEL = """
        ## How is %s's name written on the ${labelContentMarriageOrCivilPartnership} certificate?"
        """;

    public static final String WHY_NAME_DIFFERENT_LABEL = """
        Why is %s name different to how it is written on the ${labelContentMarriageOrCivilPartnership} certificate?
        """;
    public static final String WHY_NAME_DIFFERENT_HINT = """
        You must explain the reason for the difference, for example, they changed their name or part of their name was not included on the
        ${labelContentMarriageOrCivilPartnership} certificate. If you are unable to explain the difference by providing evidence, it may
        take longer to process the application. If you indicate the reason for the difference, you will have to upload some evidence like a
        government issued ID, a passport, driving licence or birth certificate, deed poll.
        """;
    public static final String WHY_NAME_DIFFERENT_DETAILS_LABEL = """
        Please provide other details of why their name is different on the ${labelContentMarriageOrCivilPartnership} certificate
        """;

    public static final String AND_CONDITION = "%s AND %s";
    private static final String NAME_DIFFERENT = "applicant1NameDifferentToMarriageCertificate=\"Yes\"";
    private static final String OTHER_REASON_NAME_DIFFERENT = "applicant1WhyNameDifferentCONTAINS\"other\"";
    private static final String CHANGED_PARTS_OF_NAME = "applicant1WhyNameDifferentCONTAINS\"changedPartsOfName\"";
    private static final String CHANGED_NAME_IN_OTHER_WAY = "applicant1NameDifferentToMarriageCertificateMethodCONTAINS\"other\"";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant1", this::midEvent)
            .pageLabel("About the applicant")
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getFirstName,
                    "", null, String.format(FIRST_NAME_LABEL, APPLICANTS_OR_APPLICANT1S), FIRST_NAME_HINT)
                .optionalWithLabel(Applicant::getMiddleName,
                    "${labelContentApplicantsOrApplicant1s} middle name")
                .mandatoryWithLabel(Applicant::getLastName,
                    "${labelContentApplicantsOrApplicant1s} last name")
                .mandatory(Applicant::getNameDifferentToMarriageCertificate,
                    "", null,
                    String.format(IS_NAME_DIFFERENT_LABEL, APPLICANTS_OR_APPLICANT1S),
                    String.format(IS_NAME_DIFFERENT_HINT, APPLICANTS_OR_APPLICANT1S)
                )
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .label("nameOnCertificate",
                        String.format(HOW_NAME_WRITTEN_ON_CERTIFICATE_LABEL, "${labelContentTheApplicantOrApplicant1}"))
                    .mandatoryWithLabel(MarriageDetails::getApplicant1Name,"${labelContentApplicantsOrApplicant1s} full name")
                .done()
            .done()
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getWhyNameDifferent,
                    NAME_DIFFERENT,
                    null,
                    String.format(WHY_NAME_DIFFERENT_LABEL, APPLICANTS_OR_APPLICANT1S),
                    WHY_NAME_DIFFERENT_HINT
                )
                .mandatory(Applicant::getWhyNameDifferentOtherDetails,
                    andShowCondition(NAME_DIFFERENT, OTHER_REASON_NAME_DIFFERENT),
                    null,
                    WHY_NAME_DIFFERENT_DETAILS_LABEL)
                .mandatory(Applicant::getNameDifferentToMarriageCertificateMethod,
                    andShowCondition(NAME_DIFFERENT, CHANGED_PARTS_OF_NAME),
                    null,
                    "What evidence will be provided for the name change?")
                .mandatory(Applicant::getNameDifferentToMarriageCertificateOtherDetails,
                    andShowCondition(NAME_DIFFERENT, CHANGED_PARTS_OF_NAME, CHANGED_NAME_IN_OTHER_WAY),
                    null,
                    "Please provide other details of what evidence will be provided")
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
                .optionalWithLabel(Applicant::getAddressOverseas,
                    "Is ${labelContentApplicantsOrApplicant1s} address international?")
                .mandatoryWithLabel(Applicant::getNonConfidentialAddress,
                    "${labelContentApplicantsOrApplicant1s} home address")
                .label("LabelHorizontalLine1-SolAboutApplicant1", DARK_HORIZONTAL_RULE)
                .mandatory(Applicant::getContactDetailsType)
                .mandatoryWithoutDefaultValue(Applicant::getInRefuge, "applicant1ContactDetailsType=\"private\"",
                    String.format(APPLICANT_REFUGE_LABEL, THE_APPLICANT_OR_APPLICANT1))
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
