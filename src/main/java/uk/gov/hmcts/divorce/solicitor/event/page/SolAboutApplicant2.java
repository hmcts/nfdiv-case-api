package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;

import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.AND_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.FIRST_NAME_HINT;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.FIRST_NAME_LABEL;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.IS_NAME_DIFFERENT_HINT;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.IS_NAME_DIFFERENT_LABEL;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.WHY_NAME_DIFFERENT_DETAILS_LABEL;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.WHY_NAME_DIFFERENT_HINT;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.WHY_NAME_DIFFERENT_LABEL;

public class SolAboutApplicant2 implements CcdPageConfiguration {

    private static final String RESPONDENTS_OR_APPLICANT2S = "${labelContentRespondentsOrApplicant2s}";

    private static final String NAME_DIFFERENT = "applicant2NameDifferentToMarriageCertificate=\"Yes\"";
    private static final String OTHER_REASON_NAME_DIFFERENT = "applicant2WhyNameDifferentCONTAINS\"other\"";
    private static final String CHANGED_PARTS_OF_NAME = "applicant2WhyNameDifferentCONTAINS\"changedPartsOfName\"";
    private static final String CHANGED_NAME_IN_OTHER_WAY = "applicant2NameDifferentToMarriageCertificateMethodCONTAINS\"other\"";
    private static final String IF_YOU_DO_NOT_PROVIDE_EVIDENCE_LABEL = """
        If you do not provide evidence to explain the difference in the ${labelContentRespondentsOrApplicant2s} name and how it is
        written on the certificate, the conditional order will be delayed until an explanation or evidence is provided.
    """;

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutApplicant2")
            .pageLabel("About the other party")
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getFirstName,
                    "", null, String.format(FIRST_NAME_LABEL, RESPONDENTS_OR_APPLICANT2S), FIRST_NAME_HINT)
                .optionalWithLabel(Applicant::getMiddleName,
                    "${labelContentRespondentsOrApplicant2s} middle name")
                .mandatoryWithLabel(Applicant::getLastName,
                    "${labelContentRespondentsOrApplicant2s} last name")
                .mandatory(Applicant::getNameDifferentToMarriageCertificate,
                    "", null,
                    String.format(IS_NAME_DIFFERENT_LABEL, RESPONDENTS_OR_APPLICANT2S),
                    String.format(
                        IS_NAME_DIFFERENT_HINT + " " + IF_YOU_DO_NOT_PROVIDE_EVIDENCE_LABEL,
                        RESPONDENTS_OR_APPLICANT2S
                    )
                )
            .done()
            .complex(CaseData::getApplication)
                .complex(Application::getMarriageDetails)
                    .label("app2NameOnCertificate", "## How is the ${labelContentRespondentsOrApplicant2s} name written on the ${labelContentMarriageOrCivilPartnership} certificate?")
                    .mandatoryWithLabel(MarriageDetails::getApplicant2Name,"${labelContentRespondentsOrApplicant2s} full name")
                .done()
            .done()
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getWhyNameDifferent,
                    NAME_DIFFERENT,
                    null,
                    String.format(WHY_NAME_DIFFERENT_LABEL, RESPONDENTS_OR_APPLICANT2S),
                    WHY_NAME_DIFFERENT_HINT
                )
                .mandatory(Applicant::getWhyNameDifferentOtherDetails,
                    String.format(AND_CONDITION, NAME_DIFFERENT, OTHER_REASON_NAME_DIFFERENT),
                    null,
                    WHY_NAME_DIFFERENT_DETAILS_LABEL
                )
                .mandatory(Applicant::getNameDifferentToMarriageCertificateMethod,
                    String.format(AND_CONDITION, NAME_DIFFERENT, CHANGED_PARTS_OF_NAME),
                    null,
                    "What evidence will be provided for the name change?")
                .mandatory(Applicant::getNameDifferentToMarriageCertificateOtherDetails,
                    String.format(AND_CONDITION, CHANGED_PARTS_OF_NAME, CHANGED_NAME_IN_OTHER_WAY),
                    null,
                    "Please provide other details of what evidence will be provided")
            .done();
    }
}
