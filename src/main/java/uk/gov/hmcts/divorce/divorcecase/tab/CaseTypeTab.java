package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;

@Component
public class CaseTypeTab implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        buildStateTab(configBuilder);
        buildAosTab(configBuilder);
        buildPaymentTab(configBuilder);
        buildLanguageTab(configBuilder);
        buildDocumentsTab(configBuilder);
        buildConfidentialTab(configBuilder);
        buildMarriageCertificateTab(configBuilder);
        buildNotesTab(configBuilder);
        buildGeneralReferralTab(configBuilder);
        buildConfidentialDocumentsTab(configBuilder);
    }

    private void buildStateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("state", "State")
            .forRoles(APPLICANT_2_SOLICITOR)
            .label("LabelState", null, "#### Case State:  ${[STATE]}");
    }

    //TODO: Need to revisit this tab once the field stated in the ticket NFDIV-595 are available
    private void buildAosTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("aosDetails", "AoS")
            .forRoles(CASEWORKER_COURTADMIN_RDU, CASEWORKER_COURTADMIN_CTSC, CASEWORKER_LEGAL_ADVISOR,
                CASEWORKER_SUPERUSER, SOLICITOR)
            .showCondition("applicationType=\"soleApplication\"")
            .label("LabelAosTabOnlineResponse-Heading", null, "## This is an online AoS response")
            .field("confirmReadPetition")
            .field("jurisdictionAgree")
            .field("jurisdictionDisagreeReason")
            .field("legalProceedingsExist")
            .field("legalProceedingsDescription")
            .field("applicant2UserId")
            .field("dueDate")
            .label("LabelAosTabOnlineResponse-RespondentRepresent", null, "### Respondent")
            .field("applicant2SolicitorRepresented")
            .field("digitalNoticeOfProceedings")
            .field("noticeOfProceedingsEmail")
            .field("noticeOfProceedingsSolicitorFirm");
    }

    private void buildPaymentTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .field("applicant1HWFReferenceNumber");
    }

    private void buildLanguageTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("languageDetails", "Language")
            .label("LabelLanguageDetails-Applicant", null, "### The applicant")
            .field("applicant1LanguagePreferenceWelsh")
            .label("LabelLanguageDetails-Respondent", null, "### The respondent")
            .field("applicant2LanguagePreferenceWelsh");
    }

    private void buildDocumentsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("documents", "Documents")
            .field(CaseData::getDocumentsGenerated)
            .field(CaseData::getApplicant1DocumentsUploaded)
            .field(CaseData::getGeneralOrders)
            .field(CaseData::getDocumentsUploaded);
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

    private void buildMarriageCertificateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("marriageDetails", "Marriage Certificate")
            .field("labelContentTheApplicant2UC", "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field("marriageApplicant1Name")
            .field("marriageApplicant2Name")
            .field("marriageDate")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCountryOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCertifyMarriageCertificateIsCorrect")
            .field("marriageMarriageCertificateIsIncorrectDetails", "marriageCertifyMarriageCertificateIsCorrect=\"No\"")
            .field("marriageIssueApplicationWithoutMarriageCertificate", "marriageCertifyMarriageCertificateIsCorrect=\"No\"");
    }

    private void buildNotesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("notes", "Notes")
            .field(CaseData::getNotes);
    }

    private void buildGeneralReferralTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("generalReferral", "General Referral")
            .forRoles(CASEWORKER_COURTADMIN_RDU, CASEWORKER_COURTADMIN_CTSC, CASEWORKER_LEGAL_ADVISOR, CASEWORKER_SUPERUSER)
            .field("generalReferralReason")
            .field("generalApplicationFrom", "generalApplicationFrom=\"*\"")
            .field("generalApplicationReferralDate", "generalApplicationReferralDate=\"*\"")
            .field("generalApplicationAddedDate")
            .field("generalReferralType")
            .field("alternativeServiceMedium")
            .field("generalReferralJudgeDetails")
            .field("generalReferralLegalAdvisorDetails")
            .field("generalReferralFeeRequired");
    }

    private void buildConfidentialDocumentsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("confidentialDocuments", "Confidential Document")
            .forRoles(CASEWORKER_COURTADMIN_RDU, CASEWORKER_COURTADMIN_CTSC, CASEWORKER_LEGAL_ADVISOR, CASEWORKER_SUPERUSER)
            .field(CaseData::getConfidentialDocumentsUploaded);
    }
}
