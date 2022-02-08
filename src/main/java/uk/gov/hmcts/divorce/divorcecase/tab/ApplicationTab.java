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

    private static final String APPLICANT_2_CONTACT_DETAILS_TYPE_PRIVATE = "applicant2ContactDetailsType!=\"private\"";
    private static final String APPLICANT_1_CONTACT_DETAILS_TYPE_PUBLIC = "applicant1ContactDetailsType=\"public\"";
    private static final String APPLICATION_TYPE_NEVER_SHOW = "applicationType=\"NEVER_SHOW\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilder = configBuilder.tab("applicationDetails", "Application");

        addHeaderFields(tabBuilder);
        addApplicant1(tabBuilder);
        addApplicant2(tabBuilder);
        addMarriageAndCertificate(tabBuilder);
        addLegalConnections(tabBuilder);
        addOtherProceedings(tabBuilder);
        addService(tabBuilder);
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
            .field(CaseData::getHyphenatedCaseRef, APPLICATION_TYPE_NEVER_SHOW);
    }

    private void addApplicant1(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant1-Heading", null, "### The applicant")
            .field("applicant1FirstName")
            .field("applicant1MiddleName")
            .field("applicant1LastName")
            .field("applicant1Gender")
            .field("applicant1NameDifferentToMarriageCertificate")
            .field("applicant1NameChangedHow", "applicant1NameDifferentToMarriageCertificate=\"Yes\"")
            .field("applicant1NameChangedHowOtherDetails", "applicant1NameChangedHow=\"other\"")
            .field("applicant1ContactDetailsType", APPLICATION_TYPE_NEVER_SHOW)
            .field("divorceWho")
            .field("applicant1ScreenHasMarriageBroken")
            .field("applicant1PcqId")
            .label("LabelApplicant1DetailsAreConfidential-Heading",
                "applicant1ContactDetailsType=\"private\"",
                "#### The applicant's contact details are confidential")
            .field("applicant1PhoneNumber", APPLICANT_1_CONTACT_DETAILS_TYPE_PUBLIC)
            .field("applicant1Email", APPLICANT_1_CONTACT_DETAILS_TYPE_PUBLIC)
            .field("applicant1HomeAddress", APPLICANT_1_CONTACT_DETAILS_TYPE_PUBLIC)
            .field("applicant1CorrespondenceAddress", APPLICANT_1_CONTACT_DETAILS_TYPE_PUBLIC)
            .field("applicant1IsApplicant2Represented")

            //Applicant 1 Solicitor
            .field("applicant1SolicitorRepresented", APPLICATION_TYPE_NEVER_SHOW)
            .label("LabelApplicant1sSolicitor-Heading",
                "applicant1SolicitorRepresented=\"Yes\"",
                "#### The applicant's solicitor")
            .field("applicant1SolicitorReference", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorName", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorAddress", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorPhone", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorEmail", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorOrganisationPolicy", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorAgreeToReceiveEmails", "applicant1SolicitorRepresented=\"Yes\"");
    }

    private void addApplicant2(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant2-Heading", null, "### ${labelContentTheApplicant2UC}")
            .field("applicant2FirstName")
            .field("applicant2MiddleName")
            .field("applicant2LastName")
            .field("applicant2Gender")
            .field("applicant2NameDifferentToMarriageCertificate")
            .field("applicant2ScreenHasMarriageBroken")
            .field("applicant2NameChangedHow", "applicant2NameDifferentToMarriageCertificate=\"Yes\"")
            .field("applicant2NameChangedHowOtherDetails", "applicant2NameChangedHow=\"other\"")
            .field("applicant2ContactDetailsType", APPLICATION_TYPE_NEVER_SHOW)
            .label("LabelApplicant2DetailsAreConfidential-Heading",
                "applicant2ContactDetailsType=\"private\"",
                "#### ${labelContentTheApplicant2UC}'s contact details are confidential")
            .label("LabelApplicant2DetailsAreShareable-Heading",
                APPLICANT_2_CONTACT_DETAILS_TYPE_PRIVATE,
                "#### ${labelContentTheApplicant2UC}'s contact details may be shared")
            .field("applicant2PhoneNumber", APPLICANT_2_CONTACT_DETAILS_TYPE_PRIVATE)
            .field("applicant2Email", APPLICANT_2_CONTACT_DETAILS_TYPE_PRIVATE)
            .field("applicant2HomeAddress", APPLICANT_2_CONTACT_DETAILS_TYPE_PRIVATE)
            .field("applicant2CorrespondenceAddress", APPLICANT_2_CONTACT_DETAILS_TYPE_PRIVATE)
            .field("applicant2AgreedToReceiveEmails")

            //Applicant 2 Solicitor
            .field("applicant2SolicitorRepresented", APPLICATION_TYPE_NEVER_SHOW)
            .label("LabelApplicant2sSolicitor-Heading",
                "applicant2SolicitorRepresented=\"Yes\"",
                "#### ${labelContentTheApplicant2UC}'s solicitor")
            .field("applicant2SolicitorReference", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorName", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorAddress", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorPhone", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorEmail", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorOrganisationPolicy", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorAgreeToReceiveEmailsCheckbox", "applicant2SolicitorRepresented=\"Yes\"");
    }

    private void addMarriageAndCertificate(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelMarriage-Heading", null, "### Marriage and certificate")
            .field("labelContentTheApplicant2UC", APPLICATION_TYPE_NEVER_SHOW)
            .field("labelContentTheApplicant2", APPLICATION_TYPE_NEVER_SHOW)
            .field("marriageDate")
            .field("marriageApplicant1Name")
            .field("marriageApplicant2Name")
            .field("marriageFormationType")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCountryOfMarriage", "marriageMarriedInUk=\"No\"")
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
            .label("LabelOtherProceedings-Heading", null, "### Applicant's other proceedings:")
            .field("applicant1LegalProceedings")
            .field("applicant1LegalProceedingsDetails", "applicant1LegalProceedings=\"Yes\"")
            .field("applicant1FinancialOrder")
            .field("solUrgentCase")
            .field("solUrgentCaseSupportingInformation", "solUrgentCase=\"Yes\"")
            .field("solStatementOfReconciliationCertify")
            .field("solStatementOfReconciliationDiscussed")
            .field("applicant1StatementOfTruth")
            .field("solSignStatementOfTruth")
            .field("solStatementOfReconciliationName")
            .field("solStatementOfReconciliationFirm")
            .field("statementOfReconciliationComments");
    }

    private void addService(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("Label-SolicitorService", "solServiceMethod=\"solicitorService\"", "### Solicitor Service")
            .field("solServiceMethod", "solServiceMethod=\"*\"")
            .field("solServiceDateOfService", "solServiceMethod=\"solicitorService\"")
            .field("solServiceDocumentsServed", "solServiceMethod=\"solicitorService\"")
            .field("solServiceOnWhomServed", "solServiceMethod=\"solicitorService\"")
            .field("solServiceHowServed", "solServiceMethod=\"solicitorService\"")
            .field("solServiceServiceDetails",
                "solServiceHowServed=\"deliveredTo\" OR solServiceHowServed=\"postedTo\"")
            .field("solServiceAddressServed", "solServiceMethod=\"solicitorService\"")
            .field("solServiceBeingThe", "solServiceMethod=\"solicitorService\"")
            .field("solServiceLocationServed", "solServiceMethod=\"solicitorService\"")
            .field("solServiceSpecifyLocationServed", "solServiceMethod=\"solicitorService\" AND solServiceLocationServed=\"otherSpecify\"")
            .field("solServiceServiceSotName", "solServiceMethod=\"solicitorService\"")
            .field("solServiceTruthStatement", "solServiceMethod=\"solicitorService\" AND solServiceHowServed=\"*\"")
            .field("solServiceServiceSotFirm", "solServiceMethod=\"solicitorService\"");
    }
}
