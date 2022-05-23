package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Tab;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

@Component
public class ApplicationTab implements CCDConfig<CaseData, State, UserRole> {

    private static final String APPLICANT_1_CONTACT_DETAILS_PUBLIC = "applicant1ContactDetailsType!=\"private\"";
    private static final String APPLICANT_2_CONTACT_DETAILS_PUBLIC = "applicant2ContactDetailsType!=\"private\"";
    private static final String NEVER_SHOW = "applicationType=\"NEVER_SHOW\"";
    private static final String JOINT_APPLICATION = "applicationType=\"jointApplication\"";
    private static final String SOLE_APPLICATION = "applicationType=\"soleApplication\"";
    private static final String NOT_NEW_PAPER_CASE = "newPaperCase!=\"Yes\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        buildSoleApplicationTab(configBuilder);
        buildJointApplicationTab(configBuilder);
    }

    private void buildSoleApplicationTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilderForSoleApplication = configBuilder.tab("applicationDetailsSole", "Application")
            .showCondition("applicationType=\"soleApplication\"");

        addDynamicContentHiddenFields(tabBuilderForSoleApplication);
        addHeaderFields(tabBuilderForSoleApplication);
        addApplicant1(tabBuilderForSoleApplication);
        addApplicant2(tabBuilderForSoleApplication);
        addMarriageAndCertificate(tabBuilderForSoleApplication);
        addLegalConnections(tabBuilderForSoleApplication);
        addOtherProceedings(tabBuilderForSoleApplication);
        addService(tabBuilderForSoleApplication);
        addOtherCourtCases(tabBuilderForSoleApplication);
        addApplicant1StatementOfTruth(tabBuilderForSoleApplication);
    }

    private void buildJointApplicationTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilderForJointApplication = configBuilder.tab("applicationDetailsJoint", "Application")
            .showCondition("applicationType=\"jointApplication\"");

        addDynamicContentHiddenFields(tabBuilderForJointApplication);
        addHeaderFields(tabBuilderForJointApplication);
        addApplicant1(tabBuilderForJointApplication);
        addOtherCourtCases(tabBuilderForJointApplication);
        addApplicant1StatementOfTruth(tabBuilderForJointApplication);
        addMarriageAndCertificate(tabBuilderForJointApplication);
        addLegalConnections(tabBuilderForJointApplication);
        addApplicant2(tabBuilderForJointApplication);
        addOtherProceedings(tabBuilderForJointApplication);
        addService(tabBuilderForJointApplication);
    }

    private void addDynamicContentHiddenFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("labelContentTheApplicantOrApplicant1", NEVER_SHOW)
            .field("labelContentTheApplicantOrApplicant1UC", NEVER_SHOW)
            .field("labelContentApplicantsOrApplicant1s", NEVER_SHOW)
            .field("labelContentTheApplicant2", NEVER_SHOW)
            .field("labelContentTheApplicant2UC", NEVER_SHOW)
            .field("labelContentGotMarriedOrFormedCivilPartnership", NEVER_SHOW)
            .field("labelContentMarriageOrCivilPartnership", NEVER_SHOW)
            .field("labelContentMarriageOrCivilPartnershipUC", NEVER_SHOW);
    }

    private void addHeaderFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("createdDate")
            .field("dateSubmitted")
            .field("issueDate")
            .field("dueDate")
            .field(CaseData::getApplicationType)
            .field(CaseData::getDivorceOrDissolution)
            .field(CaseData::getDivorceUnit)
            .field(CaseData::getBulkListCaseReference)
            .field(CaseData::getHyphenatedCaseRef, NEVER_SHOW);
    }

    private void addApplicant1(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant1-Heading", null, "### ${labelContentTheApplicantOrApplicant1UC}")
            .field("applicant1FirstName")
            .field("applicant1MiddleName")
            .field("applicant1LastName")
            .field("applicant1Gender")
            .field("newPaperCase", NEVER_SHOW)
            .field("marriageFormationType", NOT_NEW_PAPER_CASE)
            .field("applicant1LastNameChangedWhenMarried")
            .field("applicant1NameDifferentToMarriageCertificate")
            .field("applicant1NameChangedHow")
            .field("applicant1NameChangedHowOtherDetails")
            .field("applicant1ContactDetailsType", NEVER_SHOW)
            .field("divorceWho")
            .field("applicant1HasMarriageBroken", "divorceOrDissolution=\"divorce\"")
            .field("applicant1HasCivilPartnershipBroken", "divorceOrDissolution=\"dissolution\"")
            .field("applicant1PcqId")
            .field("applicant1Offline")
            .label("LabelApplicant1DetailsAreConfidential-Heading",
                "applicant1ContactDetailsType=\"private\"",
                "#### ${labelContentTheApplicantOrApplicant1UC}'s contact details are confidential")
            .field("applicant1PhoneNumber", APPLICANT_1_CONTACT_DETAILS_PUBLIC)
            .field("applicant1Email", APPLICANT_1_CONTACT_DETAILS_PUBLIC)
            .field("applicant1Address", APPLICANT_1_CONTACT_DETAILS_PUBLIC)
            .field("applicant1CannotUpload")
            .field("applicant1CannotUploadSupportingDocument")

            //Applicant 1 Solicitor
            .field("applicant1SolicitorRepresented", NEVER_SHOW)
            .label("LabelApplicant1sSolicitor-Heading",
                "applicant1SolicitorRepresented=\"Yes\"",
                "#### ${labelContentTheApplicantOrApplicant1UC}'s solicitor")
            .field("applicant1SolicitorReference", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorName", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorAddress", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorPhone", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorEmail", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorOrganisationPolicy", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorAgreeToReceiveEmailsCheckbox", "applicant1SolicitorRepresented=\"Yes\"");
    }

    private void addOtherCourtCases(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant1OtherProceedings-Heading", null, "#### ${labelContentTheApplicantOrApplicant1UC}'s other proceedings:")
            .field("applicant1LegalProceedings")
            .field("applicant1LegalProceedingsDetails",
                "applicant1LegalProceedings=\"Yes\"")
            .field("applicant1FinancialOrder")
            .field("applicant1FinancialOrdersFor",
                "applicant1FinancialOrder=\"Yes\"");
    }

    private void addApplicant2(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant2-Heading", null, "### ${labelContentTheApplicant2UC}")
            .field("applicant2FirstName")
            .field("applicant2MiddleName")
            .field("applicant2LastName")
            .field("applicant2Gender")
            .field("applicant2LastNameChangedWhenMarried")
            .field("applicant2NameDifferentToMarriageCertificate")
            .field("applicant2NameChangedHow")
            .field("applicant2NameChangedHowOtherDetails")
            .field("applicant2ContactDetailsType", NEVER_SHOW)
            .field("applicant2ScreenHasMarriageBroken")
            .field("applicant2PcqId")
            .field("applicant2Offline")
            .label("LabelApplicant2DetailsAreConfidential-Heading",
                "applicant2ContactDetailsType=\"private\"",
                "#### ${labelContentTheApplicant2UC}'s contact details are confidential")
            .field("applicant2PhoneNumber", APPLICANT_2_CONTACT_DETAILS_PUBLIC)
            .field("applicant2Email", APPLICANT_2_CONTACT_DETAILS_PUBLIC)
            .field("applicant1IsApplicant2Represented")
            .field("applicant2Address", APPLICANT_2_CONTACT_DETAILS_PUBLIC)
            .field("applicant2AgreedToReceiveEmails")
            .field("applicant2CannotUpload")
            .field("applicant2CannotUploadSupportingDocument")

            //Applicant 2 Solicitor
            .field("applicant2SolicitorRepresented", NEVER_SHOW)
            .label("LabelApplicant2sSolicitor-Heading",
                "applicant2SolicitorRepresented=\"Yes\" OR applicant1IsApplicant2Represented=\"Yes\"",
                "#### ${labelContentTheApplicant2UC}'s solicitor")
            .field("applicant2SolicitorReference", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorName", "applicant2SolicitorRepresented=\"Yes\" OR applicant1IsApplicant2Represented=\"Yes\"")
            .field("applicant2SolicitorAddress", "applicant2SolicitorRepresented=\"Yes\" OR applicant1IsApplicant2Represented=\"Yes\"")
            .field("applicant2SolicitorPhone", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorEmail", "applicant2SolicitorRepresented=\"Yes\" OR applicant1IsApplicant2Represented=\"Yes\"")
            .field("applicant2SolicitorFirmName", "applicant2SolicitorRepresented=\"Yes\" OR applicant1IsApplicant2Represented=\"Yes\"")
            .field("applicant2SolicitorOrganisationPolicy", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorAgreeToReceiveEmailsCheckbox", "applicant2SolicitorRepresented=\"Yes\"")

            //Applicant 2 Other proceedings
            .label("LabelApplicant2OtherProceedings-Heading",
                JOINT_APPLICATION,
                "#### Applicant 2's other proceedings:")
            .field("applicant2LegalProceedings", JOINT_APPLICATION)
            .field("applicant2LegalProceedingsDetails",
                "applicant2LegalProceedings=\"Yes\" AND applicationType=\"jointApplication\"")
            .field("applicant2FinancialOrder", JOINT_APPLICATION)
            .field("applicant2FinancialOrdersFor",
                "applicant2FinancialOrder=\"Yes\" AND applicationType=\"jointApplication\"")

            .field("applicant2StatementOfTruth", JOINT_APPLICATION);
    }

    private void addMarriageAndCertificate(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelMarriage-Heading",
                "divorceOrDissolution = \"divorce\"", "### Marriage and certificate")
            .label("LabelCivilPartnership-Heading",
                "divorceOrDissolution = \"dissolution\"",
                "### Civil partnership and certificate")
            .field("marriageDate")
            .field("marriageApplicant1Name")
            .field("marriageApplicant2Name")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage",
                "marriageMarriedInUk=\"No\" OR marriagePlaceOfMarriage=\"*\"")
            .field("marriageCountryOfMarriage",
                "marriageMarriedInUk=\"No\" OR marriageCountryOfMarriage=\"*\"")
            .field("marriageCertificateInEnglish")
            .field("marriageCertifiedTranslation", "marriageCertificateInEnglish=\"No\"");
    }

    private void addLegalConnections(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelJurisdiction-Heading", null, "### Jurisdiction")
            .field("jurisdictionConnections");
    }

    private void addOtherProceedings(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("solUrgentCase")
            .field("solUrgentCaseSupportingInformation", "solUrgentCase=\"Yes\"")
            .field("solStatementOfReconciliationCertify")
            .field("solStatementOfReconciliationDiscussed")
            .field("solSignStatementOfTruth")
            .field("solStatementOfReconciliationName")
            .field("solStatementOfReconciliationFirm")
            .field("statementOfReconciliationComments");
    }

    private void addService(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("Label-SolicitorService", "serviceMethod=\"solicitorService\"", "### Solicitor Service")
            .field("serviceMethod", SOLE_APPLICATION)
            .field("solServiceDateOfService", "serviceMethod=\"solicitorService\"")
            .field("solServiceDocumentsServed", "serviceMethod=\"solicitorService\"")
            .field("solServiceOnWhomServed", "serviceMethod=\"solicitorService\"")
            .field("solServiceHowServed", "serviceMethod=\"solicitorService\"")
            .field("solServiceServiceDetails",
                "solServiceHowServed=\"deliveredTo\" OR solServiceHowServed=\"postedTo\"")
            .field("solServiceAddressServed", "serviceMethod=\"solicitorService\"")
            .field("solServiceBeingThe", "serviceMethod=\"solicitorService\"")
            .field("solServiceLocationServed", "serviceMethod=\"solicitorService\"")
            .field("solServiceSpecifyLocationServed",
                "serviceMethod=\"solicitorService\" AND solServiceLocationServed=\"otherSpecify\"")
            .field("solServiceServiceSotName", "serviceMethod=\"solicitorService\"")
            .field("solServiceTruthStatement", "serviceMethod=\"solicitorService\" AND solServiceHowServed=\"*\"")
            .field("solServiceServiceSotFirm", "serviceMethod=\"solicitorService\"");
    }

    private void addApplicant1StatementOfTruth(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("applicant1StatementOfTruth");
    }
}
