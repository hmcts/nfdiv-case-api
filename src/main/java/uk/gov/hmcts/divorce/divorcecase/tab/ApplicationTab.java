package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Tab;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.SeparationOrderGranted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.notShowForState;

@Component
public class ApplicationTab implements CCDConfig<CaseData, State, UserRole> {

    private static final String APPLICANT_1_CONTACT_DETAILS_PUBLIC = "applicant1ContactDetailsType!=\"private\"";
    private static final String APPLICANT_1_CONTACT_DETAILS_PUBLIC_OVERSEAS = "applicant1ContactDetailsType!=\"private\" AND "
        + "applicant1AddressOverseas=\"Yes\"";
    private static final String APPLICANT_1_REPRESENTED_OVERSEAS = "applicant1SolicitorRepresented=\"Yes\" AND "
        + "applicant1SolicitorAddressOverseas=\"Yes\"";
    private static final String APPLICANT_2_CONTACT_DETAILS_PUBLIC = "applicant2ContactDetailsType!=\"private\"";
    private static final String APPLICANT_2_CONTACT_DETAILS_PUBLIC_OVERSEAS = "applicant2ContactDetailsType!=\"private\" AND "
        + "applicant2AddressOverseas=\"Yes\"";
    private static final String APPLICANT_2_REPRESENTED_OVERSEAS = "applicant2SolicitorRepresented=\"Yes\" AND "
        + "applicant2SolicitorAddressOverseas=\"Yes\"";
    private static final String NEVER_SHOW = "applicationType=\"NEVER_SHOW\"";
    private static final String JOINT_APPLICATION = "applicationType=\"jointApplication\"";
    private static final String SOLE_APPLICATION = "applicationType=\"soleApplication\"";
    private static final String NOT_NEW_PAPER_CASE = "newPaperCase!=\"Yes\"";
    private static final String NOT_JS_OR_NULLITY_CASE = "supplementaryCaseType=\"notApplicable\"";
    private static final String JS_OR_NULLITY_CASE = "supplementaryCaseType!=\"notApplicable\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        buildSoleApplicationTabWithAllContactDetails(configBuilder);
        buildSoleApplicationTabWithApplicant1ContactDetails(configBuilder);
        buildSoleApplicationTabWithApplicant2ContactDetails(configBuilder);

        buildJointApplicationTabWithAllContactDetails(configBuilder);
        buildJointApplicationTabWithApplicant1ContactDetails(configBuilder);
        buildJointApplicationTabWithApplicant2ContactDetails(configBuilder);
    }

    private void buildSoleApplicationTabWithAllContactDetails(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilderForSoleApplication = configBuilder.tab("applicationDetailsSole", "Application")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .showCondition("applicationType=\"soleApplication\"");

        addDynamicContentHiddenAndHeaderFields(tabBuilderForSoleApplication);

        addApplicant1PersonalDetails(tabBuilderForSoleApplication);
        addApplicant1ContactDetails(tabBuilderForSoleApplication);
        addApplicant1Representation(tabBuilderForSoleApplication);

        addApplicant2PersonalDetails(tabBuilderForSoleApplication);
        addApplicant2ContactDetails(tabBuilderForSoleApplication);
        addApplicant2Representation(tabBuilderForSoleApplication);

        addSoleApplicationFields(tabBuilderForSoleApplication);
    }

    private void buildSoleApplicationTabWithApplicant1ContactDetails(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilderForSoleApplication = configBuilder.tab(
            "applicationDetailsSoleApplicant1", "Application"
            ).forRoles(APPLICANT_1_SOLICITOR)
            .showCondition("applicationType=\"soleApplication\"");

        addDynamicContentHiddenAndHeaderFields(tabBuilderForSoleApplication);

        addApplicant1PersonalDetails(tabBuilderForSoleApplication);
        addApplicant1ContactDetails(tabBuilderForSoleApplication);
        addApplicant1Representation(tabBuilderForSoleApplication);

        addApplicant2PersonalDetails(tabBuilderForSoleApplication);
        addApplicant2Representation(tabBuilderForSoleApplication);

        addSoleApplicationFields(tabBuilderForSoleApplication);
    }

    private void buildSoleApplicationTabWithApplicant2ContactDetails(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilderForSoleApplication = configBuilder.tab(
            "applicationDetailsSoleApplicant2", "Application"
            ).forRoles(APPLICANT_2_SOLICITOR)
            .showCondition("applicationType=\"soleApplication\"");

        addDynamicContentHiddenAndHeaderFields(tabBuilderForSoleApplication);

        addApplicant1PersonalDetails(tabBuilderForSoleApplication);
        addApplicant1Representation(tabBuilderForSoleApplication);

        addApplicant2PersonalDetails(tabBuilderForSoleApplication);
        addApplicant2ContactDetails(tabBuilderForSoleApplication);
        addApplicant2Representation(tabBuilderForSoleApplication);

        addSoleApplicationFields(tabBuilderForSoleApplication);
    }

    private void buildJointApplicationTabWithAllContactDetails(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilderForJointApplication = configBuilder.tab(
            "applicationDetailsJoint", "Application"
            ).forRoles(CASE_WORKER, LEGAL_ADVISOR, JUDGE, SUPER_USER)
            .showCondition("applicationType=\"jointApplication\"");

        addDynamicContentHiddenAndHeaderFields(tabBuilderForJointApplication);
        addApplicant1PersonalDetails(tabBuilderForJointApplication);
        addApplicant1ContactDetails(tabBuilderForJointApplication);
        addApplicant1Representation(tabBuilderForJointApplication);
        addOtherCourtCases(tabBuilderForJointApplication);
        addApplicant1StatementOfTruth(tabBuilderForJointApplication);
        addMarriageAndCertificate(tabBuilderForJointApplication);
        addLegalConnections(tabBuilderForJointApplication);
        addApplicant2PersonalDetails(tabBuilderForJointApplication);
        addApplicant2ContactDetails(tabBuilderForJointApplication);
        addApplicant2Representation(tabBuilderForJointApplication);
        addOtherProceedings(tabBuilderForJointApplication);
        addService(tabBuilderForJointApplication);
    }

    private void buildJointApplicationTabWithApplicant1ContactDetails(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilderForJointApplication = configBuilder.tab(
            "applicationDetailsJointApplicant1", "Application"
            ).forRoles(APPLICANT_1_SOLICITOR)
            .showCondition("applicationType=\"jointApplication\"");

        addDynamicContentHiddenAndHeaderFields(tabBuilderForJointApplication);
        addApplicant1PersonalDetails(tabBuilderForJointApplication);
        addApplicant1ContactDetails(tabBuilderForJointApplication);
        addApplicant1Representation(tabBuilderForJointApplication);
        addOtherCourtCases(tabBuilderForJointApplication);
        addApplicant1StatementOfTruth(tabBuilderForJointApplication);
        addMarriageAndCertificate(tabBuilderForJointApplication);
        addLegalConnections(tabBuilderForJointApplication);
        addApplicant2PersonalDetails(tabBuilderForJointApplication);
        addApplicant2Representation(tabBuilderForJointApplication);
        addOtherProceedings(tabBuilderForJointApplication);
        addService(tabBuilderForJointApplication);
    }

    private void buildJointApplicationTabWithApplicant2ContactDetails(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final Tab.TabBuilder<CaseData, UserRole> tabBuilderForJointApplication = configBuilder.tab(
            "applicationDetailsJointApplicant2", "Application"
            ).forRoles(APPLICANT_2_SOLICITOR)
            .showCondition("applicationType=\"jointApplication\"");

        addDynamicContentHiddenAndHeaderFields(tabBuilderForJointApplication);
        addApplicant1PersonalDetails(tabBuilderForJointApplication);
        addApplicant1Representation(tabBuilderForJointApplication);
        addOtherCourtCases(tabBuilderForJointApplication);
        addApplicant1StatementOfTruth(tabBuilderForJointApplication);
        addMarriageAndCertificate(tabBuilderForJointApplication);
        addLegalConnections(tabBuilderForJointApplication);
        addApplicant2PersonalDetails(tabBuilderForJointApplication);
        addApplicant2ContactDetails(tabBuilderForJointApplication);
        addApplicant2Representation(tabBuilderForJointApplication);
        addOtherProceedings(tabBuilderForJointApplication);
        addService(tabBuilderForJointApplication);
    }

    private void addDynamicContentHiddenAndHeaderFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        addDynamicContentHiddenFields(tabBuilder);
        addHeaderFields(tabBuilder);
    }

    private void addSoleApplicationFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        addMarriageAndCertificate(tabBuilder);
        addLegalConnections(tabBuilder);
        addOtherProceedings(tabBuilder);
        addService(tabBuilder);
        addOtherCourtCases(tabBuilder);
        addApplicant1StatementOfTruth(tabBuilder);
    }

    private void addDynamicContentHiddenFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("labelContentTheApplicantOrApplicant1", NEVER_SHOW)
            .field("labelContentTheApplicantOrApplicant1UC", NEVER_SHOW)
            .field("labelContentApplicantsOrApplicant1s", NEVER_SHOW)
            .field("labelContentTheApplicant2", NEVER_SHOW)
            .field("labelContentTheApplicant2UC", NEVER_SHOW)
            .field("labelContentApplicant2UC", NEVER_SHOW)
            .field("labelContentGotMarriedOrFormedCivilPartnership", NEVER_SHOW)
            .field("labelContentMarriageOrCivilPartnership", NEVER_SHOW)
            .field("labelContentMarriageOrCivilPartnershipUC", NEVER_SHOW);
    }

    private void addHeaderFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("createdDate")
            .field("dateSubmitted")
            .field("issueDate")
            .field("dueDate", notShowForState(SeparationOrderGranted))
            .field(CaseData::getApplicationType)
            .field(CaseData::getDivorceOrDissolution, NOT_JS_OR_NULLITY_CASE)
            .field(CaseData::getSupplementaryCaseType, JS_OR_NULLITY_CASE)
            .field(CaseData::getDivorceUnit)
            .field(CaseData::getBulkListCaseReferenceLink)
            .field(CaseData::getHyphenatedCaseRef, NEVER_SHOW);
    }

    private void addApplicant1PersonalDetails(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant1-Heading", null, "### ${labelContentTheApplicantOrApplicant1UC}")
            .field("applicant1FirstName")
            .field("applicant1MiddleName")
            .field("applicant1LastName")
            .field("applicant1Gender")
            .field("newPaperCase", NEVER_SHOW)
            .field("marriageFormationType", NOT_NEW_PAPER_CASE)
            .field("applicant1LastNameChangedWhenMarried")
            .field("applicant1LastNameChangedWhenMarriedMethod")
            .field("applicant1LastNameChangedWhenMarriedOtherDetails")
            .field("applicant1NameDifferentToMarriageCertificate")
            .field("applicant1NameDifferentToMarriageCertificateMethod")
            .field("applicant1NameDifferentToMarriageCertificateOtherDetails")
            .field("applicant1NameChangedHow")
            .field("applicant1NameChangedHowOtherDetails")
            .field("applicant1ContactDetailsType", NEVER_SHOW)
            .field("divorceWho")
            .field("applicant1ScreenHasMarriageBroken")
            .field("applicant1PcqId")
            .field("applicant1Offline");
    }

    private void addApplicant1ContactDetails(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant1DetailsAreConfidential-Heading",
                "applicant1ContactDetailsType=\"private\"",
                "#### ${labelContentTheApplicantOrApplicant1UC}'s contact details are confidential")
            .field("applicant1PhoneNumber", APPLICANT_1_CONTACT_DETAILS_PUBLIC)
            .field("applicant1Email", APPLICANT_1_CONTACT_DETAILS_PUBLIC)
            .field("applicant1Address", APPLICANT_1_CONTACT_DETAILS_PUBLIC)
            .field("applicant1AddressOverseas", APPLICANT_1_CONTACT_DETAILS_PUBLIC_OVERSEAS);
    }

    private void addApplicant1Representation(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("applicant1CannotUpload")
            .field("applicant1CannotUploadSupportingDocument")
            .field("applicant1KnowsApplicant2Address",
                "applicant1WantsToHavePapersServedAnotherWay=\"Yes\"")
            .field("applicant1WantsToHavePapersServedAnotherWay",
                "applicant1WantsToHavePapersServedAnotherWay=\"Yes\"")

            //Applicant 1 Solicitor
            .field("applicant1SolicitorRepresented", NEVER_SHOW)
            .label("LabelApplicant1sSolicitor-Heading",
                "applicant1SolicitorRepresented=\"Yes\"",
                "#### ${labelContentTheApplicantOrApplicant1UC}'s solicitor")
            .field("applicant1SolicitorReference", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorName", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorAddress", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorAddressOverseas", APPLICANT_1_REPRESENTED_OVERSEAS)
            .field("applicant1SolicitorPhone", "applicant1SolicitorRepresented=\"Yes\"")
            .field("applicant1SolicitorEmail", "applicant1SolicitorRepresented=\"Yes\"")
            .field(
                "applicant1SolicitorOrganisationPolicy",
                "applicant1SolicitorRepresented=\"Yes\" AND applicant1SolicitorOrganisationPolicy.Organisation.OrganisationID=\"*\"")
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

    private void addApplicant2PersonalDetails(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant2-Heading", null, "### ${labelContentTheApplicant2UC}")
            .field("applicant2FirstName")
            .field("applicant2MiddleName")
            .field("applicant2LastName")
            .field("applicant2Gender")
            .field("applicant2LastNameChangedWhenMarried")
            .field("applicant2LastNameChangedWhenMarriedMethod")
            .field("applicant2LastNameChangedWhenMarriedOtherDetails")
            .field("applicant2NameDifferentToMarriageCertificate")
            .field("applicant2NameDifferentToMarriageCertificateMethod")
            .field("applicant2NameDifferentToMarriageCertificateOtherDetails")
            .field("applicant2NameChangedHow")
            .field("applicant2NameChangedHowOtherDetails")
            .field("applicant2ContactDetailsType", NEVER_SHOW)
            .field("applicant2ScreenHasMarriageBroken", "applicationType=\"jointApplication\"")
            .field("applicant2PcqId")
            .field("applicant2Offline");
    }

    private void addApplicant2ContactDetails(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant2DetailsAreConfidential-Heading",
                "applicant2ContactDetailsType=\"private\"",
                "#### ${labelContentTheApplicant2UC}'s contact details are confidential")
            .field("applicant2PhoneNumber", APPLICANT_2_CONTACT_DETAILS_PUBLIC)
            .field("applicant2Email", APPLICANT_2_CONTACT_DETAILS_PUBLIC)
            .field("applicant2Address", APPLICANT_2_CONTACT_DETAILS_PUBLIC)
            .field("applicant2AddressOverseas", APPLICANT_2_CONTACT_DETAILS_PUBLIC_OVERSEAS);
    }

    private void addApplicant2Representation(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field("applicant2AgreedToReceiveEmails")
            .field("applicant2CannotUpload")
            .field("applicant2CannotUploadSupportingDocument")
            .field("applicant1IsApplicant2Represented", "applicant2SolicitorRepresented!=\"*\"")
            .field("applicant2SolicitorRepresented")

            //Applicant 2 Solicitor
            .label("LabelApplicant2sSolicitorNewCases-Heading",
                "applicant2SolicitorRepresented=\"Yes\"",
                "#### ${labelContentApplicant2UC}'s solicitor details")
            .label("LabelApplicant2sSolicitorOldCases-Heading",
                "applicant1IsApplicant2Represented=\"Yes\" AND applicant2SolicitorRepresented!=\"*\"",
                "#### ${labelContentApplicant2UC}'s solicitor details")
            .field("applicant2SolicitorReference", "applicant2SolicitorRepresented!=\"No\"")
            .field("applicant2SolicitorName", "applicant2SolicitorRepresented!=\"No\"")
            .field("applicant2SolicitorAddress", "applicant2SolicitorRepresented!=\"No\"")
            .field("applicant2SolicitorAddressOverseas", APPLICANT_2_REPRESENTED_OVERSEAS)
            .field("applicant2SolicitorPhone", "applicant2SolicitorRepresented!=\"No\"")
            .field("applicant2SolicitorEmail", "applicant2SolicitorRepresented!=\"No\"")
            .field("applicant2SolicitorFirmName", "applicant2SolicitorRepresented!=\"No\"")
            .field(
                "applicant2SolicitorOrganisationPolicy",
                "applicant2SolicitorRepresented!=\"No\" AND applicant2SolicitorOrganisationPolicy.Organisation.OrganisationID=\"*\""
            )
            .field("applicant2SolicitorAgreeToReceiveEmailsCheckbox", "applicant2SolicitorRepresented!=\"No\"")

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
            .field("solSignStatementOfTruth", "applicant1SolicitorRepresented=\"Yes\"")
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
            .field("solServiceServiceProcessedByProcessServer",
                "serviceMethod=\"solicitorService\" AND solServiceServiceProcessedByProcessServer=\"*\"")
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
