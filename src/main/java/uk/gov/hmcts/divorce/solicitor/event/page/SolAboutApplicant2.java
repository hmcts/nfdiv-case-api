package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;

import static uk.gov.hmcts.divorce.common.ccd.PageBuilder.andShowCondition;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.EVIDENCE_FOR_NAME_CHANGE_LABEL;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.FIRST_NAME_HINT;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.FIRST_NAME_LABEL;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.HOW_NAME_WRITTEN_ON_CERTIFICATE_LABEL;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.IS_NAME_DIFFERENT_HINT;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.IS_NAME_DIFFERENT_LABEL;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.WHY_NAME_DIFFERENT_DETAILS_LABEL;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.WHY_NAME_DIFFERENT_HINT;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.WHY_NAME_DIFFERENT_LABEL;

public class SolAboutApplicant2 implements CcdPageConfiguration {

    private static final String RESPONDENTS_OR_APPLICANT2S = "${labelContentRespondentsOrApplicant2s}";
    private static final String THE_RESPONDENT_OR_APPLICANT2 = "${labelContentTheApplicant2}";

    private static final String IF_YOU_DO_NOT_PROVIDE_EVIDENCE_LABEL = """
        If you do not provide evidence to explain the difference in the ${labelContentRespondentsOrApplicant2s} name and how it is
        written on the certificate, the conditional order will be delayed until an explanation or evidence is provided.
        """;


    public static final String APP2_NAME_IS_DIFFERENT = "applicant2NameDifferentToMarriageCertificate=\"Yes\"";
    public static final String APP2_NAME_IS_DIFFERENT_FOR_OTHER_REASON = "applicant2WhyNameDifferentCONTAINS\"other\"";
    public static final String APP2_HAS_CHANGED_PARTS_OF_NAME = "applicant2WhyNameDifferentCONTAINS\"changedPartsOfName\"";
    public static final String APP2_HAS_CHANGED_NAME_IN_OTHER_WAY = "applicant2NameDifferentToMarriageCertificateMethodCONTAINS\"other\"";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant2")
            .pageLabel("About the other party")
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getFirstName,
                    ALWAYS_SHOW, NO_DEFAULT_VALUE, String.format(FIRST_NAME_LABEL, RESPONDENTS_OR_APPLICANT2S), FIRST_NAME_HINT)
                .optionalWithLabel(Applicant::getMiddleName,
                    "${labelContentRespondentsOrApplicant2s} middle name")
                .mandatoryWithLabel(Applicant::getLastName,
                    "${labelContentRespondentsOrApplicant2s} last name")
                .mandatory(Applicant::getNameDifferentToMarriageCertificate, ALWAYS_SHOW, NO_DEFAULT_VALUE,
                    String.format(IS_NAME_DIFFERENT_LABEL, THE_RESPONDENT_OR_APPLICANT2),
                    String.format(IS_NAME_DIFFERENT_HINT + " " + IF_YOU_DO_NOT_PROVIDE_EVIDENCE_LABEL, THE_RESPONDENT_OR_APPLICANT2)
                )
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .label("app2NameOnCertificate",
                        String.format(HOW_NAME_WRITTEN_ON_CERTIFICATE_LABEL, THE_RESPONDENT_OR_APPLICANT2))
                    .mandatoryWithLabel(MarriageDetails::getApplicant2Name,
                        "${labelContentRespondentsOrApplicant2s} full name")
                .done()
            .done()
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getWhyNameDifferent,
                    APP2_NAME_IS_DIFFERENT,
                    NO_DEFAULT_VALUE,
                    String.format(WHY_NAME_DIFFERENT_LABEL, THE_RESPONDENT_OR_APPLICANT2),
                    WHY_NAME_DIFFERENT_HINT
                )
                .mandatory(Applicant::getWhyNameDifferentOtherDetails,
                    andShowCondition(APP2_NAME_IS_DIFFERENT, APP2_NAME_IS_DIFFERENT_FOR_OTHER_REASON),
                    NO_DEFAULT_VALUE,
                    WHY_NAME_DIFFERENT_DETAILS_LABEL
                )
                .mandatory(Applicant::getNameDifferentToMarriageCertificateMethod,
                    andShowCondition(APP2_NAME_IS_DIFFERENT, APP2_HAS_CHANGED_PARTS_OF_NAME),
                    NO_DEFAULT_VALUE,
                    EVIDENCE_FOR_NAME_CHANGE_LABEL)
                .mandatory(Applicant::getNameDifferentToMarriageCertificateOtherDetails,
                    andShowCondition(APP2_NAME_IS_DIFFERENT, APP2_HAS_CHANGED_PARTS_OF_NAME, APP2_HAS_CHANGED_NAME_IN_OTHER_WAY),
                    NO_DEFAULT_VALUE,
                    "Please provide other details of what evidence will be provided")
            .done();
    }
}
