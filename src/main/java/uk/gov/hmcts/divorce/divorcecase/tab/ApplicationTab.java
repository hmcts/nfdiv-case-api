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

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilder = configBuilder.tab("applicationDetails", "Application");

        addHeaderFields(tabBuilder);
        addApplicant1(tabBuilder);
        addApplicant2(tabBuilder);
        addMarriageAndCertificate(tabBuilder);
        addLegalConnections(tabBuilder);
        addOtherLegalProceedings(tabBuilder);
        addFinancialOrderAndSot(tabBuilder);
        addSolicitorService(tabBuilder);
    }

    private void addHeaderFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("createdDate")
            .field("dateSubmitted")
            .field("issueDate")
            .field(CaseData::getApplicationType)
            .field(CaseData::getDivorceOrDissolution)
            .field(CaseData::getDivorceUnit);
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
            .field("applicant1ContactDetailsConfidential", "applicationType=\"NEVER_SHOW\"")
            .field("divorceWho")
            .label("LabelApplicant1DetailsAreConfidential-Heading",
                "applicant1ContactDetailsConfidential=\"keep\"",
                "#### The applicant's contact details are confidential")
            .label("LabelApplicant1DetailsAreShareable-Heading",
                "applicant1ContactDetailsConfidential=\"share\"",
                "#### The applicant's contact details may be shared")
            .field("applicant1PhoneNumber", "applicant1ContactDetailsConfidential=\"share\"")
            .field("applicant1Email", "applicant1ContactDetailsConfidential=\"share\"")
            .field("applicant1HomeAddress", "applicant1ContactDetailsConfidential=\"share\"")
            .field("applicant1CorrespondenceAddress", "applicant1ContactDetailsConfidential=\"share\"")

            //Applicant 1 Solicitor
            .field("applicant1SolicitorRepresented", "applicationType=\"NEVER_SHOW\"")
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
            .field("applicant2NameChangedHow", "applicant2NameDifferentToMarriageCertificate=\"Yes\"")
            .field("applicant2NameChangedHowOtherDetails", "applicant2NameChangedHow=\"other\"")
            .field("applicant2ContactDetailsConfidential", "applicationType=\"NEVER_SHOW\"")
            .label("LabelApplicant2DetailsAreConfidential-Heading",
                "applicant2ContactDetailsConfidential=\"keep\"",
                "#### ${labelContentTheApplicant2UC}'s contact details are confidential")
            .label("LabelApplicant2DetailsAreShareable-Heading",
                "applicant2ContactDetailsConfidential=\"share\"",
                "#### ${labelContentTheApplicant2UC}'s contact details may be shared")
            .field("applicant2PhoneNumber", "applicant2ContactDetailsConfidential=\"share\"")
            .field("applicant2Email", "applicant2ContactDetailsConfidential=\"share\"")
            .field("applicant2HomeAddress", "applicant2ContactDetailsConfidential=\"share\"")
            .field("applicant2CorrespondenceAddress", "applicant2ContactDetailsConfidential=\"share\"")
            .field("applicant2AgreedToReceiveEmails")

            //Applicant 2 Solicitor
            .field("applicant2SolicitorRepresented", "applicationType=\"NEVER_SHOW\"")
            .label("LabelApplicant2sSolicitor-Heading",
                "applicant2SolicitorRepresented=\"Yes\"",
                "#### ${labelContentTheApplicant2UC}'s solicitor")
            .field("applicant2SolicitorReference", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorName", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorAddress", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorPhone", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorEmail", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorOrganisationPolicy", "applicant2SolicitorRepresented=\"Yes\"")
            .field("applicant2SolicitorAgreeToReceiveEmails", "applicant2SolicitorRepresented=\"Yes\"");
    }

    private void addMarriageAndCertificate(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelMarriage-Heading", null, "### Marriage and certificate")
            .field("labelContentTheApplicant2UC", "applicationType=\"NEVER_SHOW\"")
            .field("labelContentTheApplicant2", "applicationType=\"NEVER_SHOW\"")
            .field("marriageDate")
            .field("marriageApplicant1Name")
            .field("marriageApplicant2Name")
            .field("marriageIsSameSexCouple")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCountryOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCertificateInEnglish")
            .field("marriageCertifiedTranslation", "marriageCertificateInEnglish=\"No\"");
    }

    private void addLegalConnections(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelJurisdiction-Heading", null, "### Jurisdiction")
            .field("jurisdictionLegalConnections");
    }

    private void addOtherLegalProceedings(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelOtherLegalProceedings-Heading", null, "### Other legal proceedings")
            .field("applicant1LegalProceedings")
            .field("applicant1LegalProceedingsRelated", "applicant1LegalProceedings=\"Yes\"")
            .field("applicant1LegalProceedingsDetails", "applicant1LegalProceedings=\"Yes\"");
    }

    private void addFinancialOrderAndSot(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelFinancialOrder-Heading", null, "### Financial order")
            .field("applicant1FinancialOrder")
            .field("applicant1FinancialOrderFor", "applicant1FinancialOrder=\"Yes\"")
            .field("applicant1StatementOfTruth")
            .field("solSignStatementOfTruth")
            .field("solStatementOfReconciliationCertify")
            .field("solStatementOfReconciliationDiscussed")
            .field("statementOfReconciliationComments")
            .field("solUrgentCase")
            .field("solUrgentCaseSupportingInformation", "solUrgentCase=\"Yes\"")
            .field("solStatementOfReconciliationName")
            .field("solStatementOfReconciliationFirm");
    }

    private void addSolicitorService(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("Label-SolicitorService", "solServiceMethod=\"solicitorService\"", "### Solicitor Service")
            .field("solServiceMethod", "solServiceMethod=\"*\"");
    }
}
