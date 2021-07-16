package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;

@Component
public class CaseTypeTab implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder.tab("applicationDetails", "Application")
            .field("LabelCreatedDate", null, "${[CREATED_DATE]}")
            .field("dateSubmitted")
            .field(CaseData::getApplicationType)
            .field(CaseData::getDivorceOrDissolution)
            .field("LabelApplicant1-Heading", null, "### The applicant")
            .field("applicant1FirstName")
            .field("applicant1MiddleName")
            .field("applicant1LastName")
            .field("applicant1Gender")
            .field("applicant1NameDifferentToMarriageCertificate")
            .field("applicant1NameChangedHow", "applicant1NameDifferentToMarriageCertificate=\"Yes\"")
            .field("applicant1NameChangedHowOtherDetails", "applicant1NameChangedHow=\"other\"")
            .field("LabelApplicant1DetailsAreConfidential-Heading",
                "applicant1ContactDetailsConfidential=\"keep\"",
                "#### The applicant's contact details are confidential")
            .field("applicant1ContactDetailsConfidential")
            .field("applicant1HomeAddress", "applicant1ContactDetailsConfidential=\"share\"")
            .field("applicant1PhoneNumber", "applicant1ContactDetailsConfidential=\"share\"")
            .field("divorceWho")
            .field("applicant1SolicitorRepresented")
            .field("LabelApplicant1sSolicitor-Heading",
                "applicant1SolicitorRepresented=\"Yes\"",
                "#### The applicant's solicitor")
            .field("applicant1SolicitorName", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorPhone", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorEmail", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorOrganisationPolicy", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorReference", "applicant1SolicitorRepresented=\"Yes\"")
            .field("LabelApplicant2-Heading", null, "### Applicant 2")
            .field("applicant2FirstName")
            .field("applicant2MiddleName")
            .field("applicant2LastName")
            .field("applicant2Gender")
            .field("applicant2NameDifferentToMarriageCertificate")
            .field("applicant2NameChangedHow", "applicant2NameDifferentToMarriageCertificate=\"Yes\"")
            .field("applicant2NameChangedHowOtherDetails", "applicant2NameChangedHow=\"other\"")
            .field("applicant2SolicitorRepresented")
            .field("LabelApplicant2sSolicitor-Heading",
                "applicant2SolicitorRepresented=\"Yes\"",
                "#### The respondent's solicitor")
            .field("applicant2SolicitorName", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorPhone", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorEmail", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorOrganisationPolicy", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorReference", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2CorrespondenceAddress")
            .field("jurisdictionLegalConnections")
            .field("LabelMarriage-Heading", null, "### Marriage and certificate")
            .field("marriageDate")
            .field("marriageIsSameSexCouple")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCountryOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCertificateInEnglish")
            .field("marriageCertifiedTranslation", "marriageCertificateInEnglish=\"No\"")
            .field("marriageApplicant1Name")
            .field("marriageApplicant2Name")
            .field("LabelClaimCosts-Heading", null, "### Claim costs")
            .field("divorceCostsClaim")
            .field("divorceClaimFrom", "divorceCostsClaim=\"Yes\"")
            .field("LabelOtherLegalProceedings-Heading", null, "### Other legal proceedings")
            .field("legalProceedings")
            .field("legalProceedingsRelated", "legalProceedings=\"Yes\"")
            .field("legalProceedingsDetails", "legalProceedings=\"Yes\"")
            .field("LabelFinancialOrder-Heading", null, "### Financial order")
            .field("applicant1FinancialOrder")
            .field("applicant1FinancialOrderFor", "applicant1FinancialOrder=\"Yes\"");

        buildAosTab(configBuilder);

        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .field("helpWithFeesReferenceNumber");

        configBuilder.tab("languageDetails", "Language")
            .field("applicant1LanguagePreferenceWelsh", null, "The applicant's language preference")
            .field("applicant2LanguagePreferenceWelsh", null, "The respondent's language preference");

        configBuilder.tab("documents", "Documents")
            .field(CaseData::getDocumentsGenerated)
            .field(CaseData::getApplicant1DocumentsUploaded);

        buildConfidentialTab(configBuilder);

        configBuilder.tab("marriageDetails", "Marriage Certificate")
            .field("marriageApplicant1Name")
            .field("marriageApplicant2Name")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCountryOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCertifyMarriageCertificateIsCorrect")
            .field("marriageMarriageCertificateIsIncorrectDetails", "marriageCertifyMarriageCertificateIsCorrect=\"No\"")
            .field("marriageIssueApplicationWithoutMarriageCertificate", "marriageCertifyMarriageCertificateIsCorrect=\"No\"");

        configBuilder.tab("notes", "Notes")
            .field(CaseData::getNotes);
    }

    private void buildConfidentialTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("Confidential", "Confidential Address")
            .forRoles(CASEWORKER_COURTADMIN_RDU, CASEWORKER_COURTADMIN_CTSC, CASEWORKER_LEGAL_ADVISOR)
            .showCondition("applicant1ContactDetailsConfidential=\"keep\"")
            .field("applicant1CorrespondenceAddress")
            .field("applicant1PhoneNumber")
            .field("applicant1Email")
            .field("applicant1HomeAddress");
    }

    //TODO: Need to revisit this tab once the field stated in the ticket NFDIV-595 are available
    private void buildAosTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("aosDetails", "AoS")
            .forRoles(CASEWORKER_COURTADMIN_RDU, CASEWORKER_COURTADMIN_CTSC, CASEWORKER_LEGAL_ADVISOR,
                CASEWORKER_SUPERUSER, SOLICITOR)
            .showCondition("applicationType=\"soleApplication\"")
            .field("LabelAosTabOnlineResponse-Heading", null,"## This is an online AoS response")
            .field("confirmReadPetition")
            .field("jurisdictionAgree")
            .field("jurisdictionDisagreeReason")
            .field("legalProceedingsExist")
            .field("legalProceedingsDescription")
            .field("applicant2UserId")
            .field("dueDate")
            .field("applicant2SolicitorRepresented", null, "Is respondent represented by a solicitor?")
            .field("digitalNoticeOfProceedings")
            .field("noticeOfProceedingsEmail")
            .field("noticeOfProceedingsSolicitorFirm");
    }
}
