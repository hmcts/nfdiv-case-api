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
        Is %s's name different from the name on the ${labelContentMarriageOrCivilPartnership} certificate?
        """;
    public static final String IS_NAME_DIFFERENT_HINT = """
        If %s's name is different on the ${labelContentMarriageOrCivilPartnership} certificate, you will have to upload some
        evidence like a government issued ID, a passport, driving licence or birth certificate, deed poll.
        """;

    public static final String HOW_NAME_WRITTEN_ON_CERTIFICATE_LABEL = """
        ## How is %s's name written on the ${labelContentMarriageOrCivilPartnership} certificate?
        """;

    public static final String WHY_NAME_DIFFERENT_LABEL = """
        Why is %s's name different to how it is written on the ${labelContentMarriageOrCivilPartnership} certificate?
        """;
    public static final String WHY_NAME_DIFFERENT_HINT = """
        You must explain the reason for the difference, for example, they changed their name or part of their name was not included on the
        ${labelContentMarriageOrCivilPartnership} certificate. If you are unable to explain the difference by providing evidence, it may
        take longer to process the application. If you indicate the reason for the difference, you will have to upload some evidence like a
        government issued ID, a passport, driving licence or birth certificate, deed poll.
        """;
    public static final String WHY_NAME_DIFFERENT_DETAILS_LABEL = """
        If you selected ‘Other’, please provide details explaining why their name is different on the
        ${labelContentMarriageOrCivilPartnership} certificate
        """;

    public static final String EVIDENCE_FOR_NAME_CHANGE_LABEL = """
        What evidence will be provided for the name change? Select an option if you have indicated that they changed their last name
        or parts of their name
        """;

    private static final String APPLICANTS_OR_APPLICANT1S = "${labelContentApplicantsOrApplicant1s}";
    private static final String THE_APPLICANT_OR_APPLICANT1 = "${labelContentTheApplicantOrApplicant1}";

    private static final String NAME_IS_DIFFERENT = "applicant1NameDifferentToMarriageCertificate=\"Yes\"";
    private static final String NAME_IS_DIFFERENT_FOR_OTHER_REASON = "applicant1WhyNameDifferentCONTAINS\"other\"";
    private static final String HAS_CHANGED_PARTS_OF_NAME = "applicant1WhyNameDifferentCONTAINS\"changedPartsOfName\"";
    private static final String HAS_CHANGED_NAME_IN_OTHER_WAY = "applicant1NameDifferentToMarriageCertificateMethodCONTAINS\"other\"";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant1", this::midEvent)
            .pageLabel("About the applicant")
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getFirstName,
                    ALWAYS_SHOW, NO_DEFAULT_VALUE, String.format(FIRST_NAME_LABEL, APPLICANTS_OR_APPLICANT1S), FIRST_NAME_HINT)
                .optionalWithLabel(Applicant::getMiddleName,
                    "${labelContentApplicantsOrApplicant1s} middle name")
                .mandatoryWithLabel(Applicant::getLastName,
                    "${labelContentApplicantsOrApplicant1s} last name")
                .mandatory(Applicant::getNameDifferentToMarriageCertificate, ALWAYS_SHOW, NO_DEFAULT_VALUE,
                    String.format(IS_NAME_DIFFERENT_LABEL, THE_APPLICANT_OR_APPLICANT1),
                    String.format(IS_NAME_DIFFERENT_HINT, THE_APPLICANT_OR_APPLICANT1)
                )
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .label("nameOnCertificate",
                        String.format(HOW_NAME_WRITTEN_ON_CERTIFICATE_LABEL, THE_APPLICANT_OR_APPLICANT1))
                    .mandatoryWithLabel(MarriageDetails::getApplicant1Name,
                        "${labelContentApplicantsOrApplicant1s} full name")
                .done()
            .done()
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getWhyNameDifferent,
                    NAME_IS_DIFFERENT,
                    NO_DEFAULT_VALUE,
                    String.format(WHY_NAME_DIFFERENT_LABEL, THE_APPLICANT_OR_APPLICANT1),
                    WHY_NAME_DIFFERENT_HINT
                )
                .mandatory(Applicant::getWhyNameDifferentOtherDetails,
                    andShowCondition(NAME_IS_DIFFERENT, NAME_IS_DIFFERENT_FOR_OTHER_REASON),
                    NO_DEFAULT_VALUE,
                    WHY_NAME_DIFFERENT_DETAILS_LABEL)
                .mandatory(Applicant::getNameDifferentToMarriageCertificateMethod,
                    andShowCondition(NAME_IS_DIFFERENT, HAS_CHANGED_PARTS_OF_NAME),
                    NO_DEFAULT_VALUE,
                    EVIDENCE_FOR_NAME_CHANGE_LABEL)
                .mandatory(Applicant::getNameDifferentToMarriageCertificateOtherDetails,
                    andShowCondition(NAME_IS_DIFFERENT, HAS_CHANGED_PARTS_OF_NAME, HAS_CHANGED_NAME_IN_OTHER_WAY),
                    NO_DEFAULT_VALUE,
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
                    String.format(APPLICANT_REFUGE_LABEL, "labelContentTheApplicantOrApplicant1"))
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
