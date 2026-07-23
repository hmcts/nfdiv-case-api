package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Tab;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.common.ccd.PageBuilder.andShowCondition;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingRefund;
import static uk.gov.hmcts.divorce.divorcecase.model.State.SeparationOrderGranted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_ADDRESS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_ADDRESS_OVERSEAS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_CANNOT_UPLOAD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_CANNOT_UPLOAD_SUPPORTING_DOCUMENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_CONTACT_DETAILS_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_FINANCIAL_ORDERS_FOR;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_GENDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_IS_APPLICANT_2_REPRESENTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_KNOWS_APPLICANT_2_ADDRESS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_LAST_NAME_CHANGED_WHEN_MARRIED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_LAST_NAME_CHANGED_WHEN_MARRIED_METHOD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_LAST_NAME_CHANGED_WHEN_MARRIED_OTHER_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_LEGAL_PROCEEDINGS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_LEGAL_PROCEEDINGS_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_NAME_CHANGED_HOW;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_NAME_CHANGED_HOW_OTHER_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_NAME_DIFFERENT_TO_MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_NAME_DIFFERENT_TO_MARRIAGE_CERTIFICATE_METHOD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_NAME_DIFFERENT_TO_MARRIAGE_CERTIFICATE_OTHER_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_OFFLINE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_ORGANISATION_POLICY;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_PCQ_ID;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_PHONE_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_SCREEN_HAS_MARRIAGE_BROKEN;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_SOLICITOR_ADDRESS_OVERSEAS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_SOLICITOR_AGREE_TO_RECEIVE_EMAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_SOLICITOR_PHONE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_SOLICITOR_REPRESENTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_WANTS_TO_HAVE_PAPERS_SERVED_ANOTHER_WAY;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_WHY_NAME_DIFFERENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_WHY_NAME_DIFFERENT_OTHER_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_ADDRESS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_ADDRESS_OVERSEAS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_AGREED_TO_RECEIVE_EMAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_CANNOT_UPLOAD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_CANNOT_UPLOAD_SUPPORTING_DOCUMENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_CONTACT_DETAILS_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_FINANCIAL_ORDERS_FOR;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_GENDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LAST_NAME_CHANGED_WHEN_MARRIED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LAST_NAME_CHANGED_WHEN_MARRIED_METHOD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LAST_NAME_CHANGED_WHEN_MARRIED_OTHER_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LEGAL_PROCEEDINGS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_LEGAL_PROCEEDINGS_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_NAME_CHANGED_HOW;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_NAME_CHANGED_HOW_OTHER_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_NAME_DIFFERENT_TO_MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_NAME_DIFFERENT_TO_MARRIAGE_CERTIFICATE_METHOD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_NAME_DIFFERENT_TO_MARRIAGE_CERTIFICATE_OTHER_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_OFFLINE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_ORGANISATION_POLICY;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_PCQ_ID;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_PHONE_NUMBER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SCREEN_HAS_MARRIAGE_BROKEN;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOLICITOR_ADDRESS_OVERSEAS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CHECKBOX;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOLICITOR_PHONE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_SOLICITOR_REPRESENTED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_WHY_NAME_DIFFERENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_WHY_NAME_DIFFERENT_OTHER_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_2_WITHDRAW_APPLICATION_REASON;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICATION_CREATED_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICATION_ISSUED_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICATION_SUBMITTED_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CW_WITHDRAW_APPLICATION_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.CW_WITHDRAW_APPLICATION_REASON;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DIVORCE_WHO;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.DUE_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.JURISDICTION_CONNECTIONS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_APPLICANTS_OR_APPLICANT_1S;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_APPLICANT_2_UC;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_GOT_MARRIED_OR_FORMED_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_MARRIAGE_OR_CIVIL_PARTNERSHIP_UC;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_THE_APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_THE_APPLICANT_2_UC;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_THE_APPLICANT_OR_APPLICANT_1;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.LABEL_CONTENT_THE_APPLICANT_OR_APPLICANT_1_UC;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_APPLICANT_1_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_APPLICANT_2_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_CERTIFICATE_IN_ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_CERTIFIED_TRANSLATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_COUNTRY_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_FORMATION_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_MARRIED_IN_UK;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.MARRIAGE_PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.NEW_PAPER_CASE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.REASON_ISSUED_WITHOUT_ADDRESS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SERVICE_METHOD;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_ADDRESS_SERVED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_BEING_THE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_DATE_OF_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_DATE_PREVIOUS_SERVICE_RETURNED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_DETAILS_OF_PREVIOUS_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_DOCUMENTS_PREVIOUSLY_RETURNED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_DOCUMENTS_SERVED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_FIRST_ATTEMPT_TO_SERVE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_HOW_SERVED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_LOCATION_SERVED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_ON_WHOM_SERVED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_SERVICE_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_SERVICE_PROCESSED_BY_PROCESS_SERVER;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_SERVICE_SOT_FIRM;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_SERVICE_SOT_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_SPECIFY_LOCATION_SERVED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SERVICE_TRUTH_STATEMENT;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_SIGN_STATEMENT_OF_TRUTH;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_STATEMENT_OF_RECONCILIATION_CERTIFY;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_STATEMENT_OF_RECONCILIATION_DISCUSSED;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_STATEMENT_OF_RECONCILIATION_FIRM;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_STATEMENT_OF_RECONCILIATION_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.SOL_URGENT_CASE_SUPPORTING_INFORMATION;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.STATEMENT_OF_RECONCILIATION_COMMENTS;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.URGENT_CASE;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.WITHDRAW_APPLICATION_REASON;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.notShowForState;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.showForState;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.APP1_HAS_CHANGED_NAME_IN_OTHER_WAY;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.APP1_NAME_IS_DIFFERENT;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1.APP1_NAME_IS_DIFFERENT_FOR_OTHER_REASON;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant2.APP2_HAS_CHANGED_NAME_IN_OTHER_WAY;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant2.APP2_NAME_IS_DIFFERENT;
import static uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant2.APP2_NAME_IS_DIFFERENT_FOR_OTHER_REASON;

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
    public static final String APP1_NAME_CHANGE_EXPLANATION_REQUIRED =
        "applicant1WhyNameDifferentCONTAINS\"changedPartsOfName\" OR applicant1WhyNameDifferent!=\"*\"";
    public static final String APP2_NAME_CHANGE_EXPLANATION_REQUIRED =
        "applicant2WhyNameDifferentCONTAINS\"changedPartsOfName\" OR applicant2WhyNameDifferent!=\"*\"";
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
        addWithdrawApplicationDetails(tabBuilderForJointApplication);
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
        addWithdrawApplicationDetails(tabBuilderForJointApplication);
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
        addWithdrawApplicationDetails(tabBuilderForJointApplication);
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
        addWithdrawApplicationDetails(tabBuilder);
    }

    private void addDynamicContentHiddenFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field(LABEL_CONTENT_THE_APPLICANT_OR_APPLICANT_1, NEVER_SHOW)
            .field(LABEL_CONTENT_THE_APPLICANT_OR_APPLICANT_1_UC, NEVER_SHOW)
            .field(LABEL_CONTENT_APPLICANTS_OR_APPLICANT_1S, NEVER_SHOW)
            .field(LABEL_CONTENT_THE_APPLICANT_2, NEVER_SHOW)
            .field(LABEL_CONTENT_THE_APPLICANT_2_UC, NEVER_SHOW)
            .field(LABEL_CONTENT_APPLICANT_2_UC, NEVER_SHOW)
            .field(LABEL_CONTENT_GOT_MARRIED_OR_FORMED_CIVIL_PARTNERSHIP, NEVER_SHOW)
            .field(LABEL_CONTENT_MARRIAGE_OR_CIVIL_PARTNERSHIP, NEVER_SHOW)
            .field(LABEL_CONTENT_MARRIAGE_OR_CIVIL_PARTNERSHIP_UC, NEVER_SHOW);
    }

    private void addHeaderFields(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field(APPLICATION_CREATED_DATE)
            .field(APPLICATION_SUBMITTED_DATE)
            .field(APPLICATION_ISSUED_DATE)
            .field(REASON_ISSUED_WITHOUT_ADDRESS)
            .field(DUE_DATE, notShowForState(SeparationOrderGranted))
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
            .field(APPLICANT_1_FIRST_NAME)
            .field(APPLICANT_1_MIDDLE_NAME)
            .field(APPLICANT_1_LAST_NAME)
            .field(APPLICANT_1_GENDER)
            .field(NEW_PAPER_CASE, NEVER_SHOW)
            .field(MARRIAGE_FORMATION_TYPE, NOT_NEW_PAPER_CASE)
            .field(APPLICANT_1_NAME_DIFFERENT_TO_MARRIAGE_CERTIFICATE)
            .field(
                APPLICANT_1_NAME_DIFFERENT_TO_MARRIAGE_CERTIFICATE_METHOD,
                andShowCondition(APP1_NAME_IS_DIFFERENT, APP1_NAME_CHANGE_EXPLANATION_REQUIRED)
            )
            .field(
                APPLICANT_1_NAME_DIFFERENT_TO_MARRIAGE_CERTIFICATE_OTHER_DETAILS,
                andShowCondition(APP1_NAME_IS_DIFFERENT, APP1_NAME_CHANGE_EXPLANATION_REQUIRED, APP1_HAS_CHANGED_NAME_IN_OTHER_WAY)
            )
            .field(APPLICANT_1_WHY_NAME_DIFFERENT, APP1_NAME_IS_DIFFERENT)
            .field(
                APPLICANT_1_WHY_NAME_DIFFERENT_OTHER_DETAILS,
                andShowCondition(APP1_NAME_IS_DIFFERENT, APP1_NAME_IS_DIFFERENT_FOR_OTHER_REASON)
            )
            .field(APPLICANT_1_LAST_NAME_CHANGED_WHEN_MARRIED)
            .field(APPLICANT_1_LAST_NAME_CHANGED_WHEN_MARRIED_METHOD)
            .field(APPLICANT_1_LAST_NAME_CHANGED_WHEN_MARRIED_OTHER_DETAILS)
            .field(APPLICANT_1_NAME_CHANGED_HOW)
            .field(APPLICANT_1_NAME_CHANGED_HOW_OTHER_DETAILS)
            .field(APPLICANT_1_CONTACT_DETAILS_TYPE, NEVER_SHOW)
            .field(DIVORCE_WHO)
            .field(APPLICANT_1_SCREEN_HAS_MARRIAGE_BROKEN)
            .field(APPLICANT_1_PCQ_ID)
            .field(APPLICANT_1_OFFLINE);
    }

    private void addApplicant1ContactDetails(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant1DetailsAreConfidential-Heading",
                "applicant1ContactDetailsType=\"private\"",
                "#### ${labelContentTheApplicantOrApplicant1UC}'s contact details are confidential")
            .field(APPLICANT_1_PHONE_NUMBER, APPLICANT_1_CONTACT_DETAILS_PUBLIC)
            .field(APPLICANT_1_EMAIL, APPLICANT_1_CONTACT_DETAILS_PUBLIC)
            .field(APPLICANT_1_ADDRESS, APPLICANT_1_CONTACT_DETAILS_PUBLIC)
            .field(APPLICANT_1_ADDRESS_OVERSEAS, APPLICANT_1_CONTACT_DETAILS_PUBLIC_OVERSEAS);
    }

    private void addApplicant1Representation(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field(APPLICANT_1_CANNOT_UPLOAD)
            .field(APPLICANT_1_CANNOT_UPLOAD_SUPPORTING_DOCUMENT)
            .field(APPLICANT_1_KNOWS_APPLICANT_2_ADDRESS, "applicant1WantsToHavePapersServedAnotherWay=\"Yes\"")
            .field(APPLICANT_1_WANTS_TO_HAVE_PAPERS_SERVED_ANOTHER_WAY, "applicant1WantsToHavePapersServedAnotherWay=\"Yes\"")

            //Applicant 1 Solicitor
            .field(APPLICANT_1_SOLICITOR_REPRESENTED, NEVER_SHOW)
            .label("LabelApplicant1sSolicitor-Heading",
                "applicant1SolicitorRepresented=\"Yes\"",
                "#### ${labelContentTheApplicantOrApplicant1UC}'s solicitor")
            .field(APPLICANT_1_SOLICITOR_REFERENCE, "applicant1SolicitorRepresented=\"Yes\"")
            .field(APPLICANT_1_SOLICITOR_NAME, "applicant1SolicitorRepresented=\"Yes\"")
            .field(APPLICANT_1_SOLICITOR_ADDRESS, "applicant1SolicitorRepresented=\"Yes\"")
            .field(APPLICANT_1_SOLICITOR_ADDRESS_OVERSEAS, APPLICANT_1_REPRESENTED_OVERSEAS)
            .field(APPLICANT_1_SOLICITOR_PHONE, "applicant1SolicitorRepresented=\"Yes\"")
            .field(APPLICANT_1_SOLICITOR_EMAIL, "applicant1SolicitorRepresented=\"Yes\"")
            .field(
                APPLICANT_1_ORGANISATION_POLICY,
                "applicant1SolicitorRepresented=\"Yes\" AND applicant1SolicitorOrganisationPolicy.Organisation.OrganisationID=\"*\"")
            .field(APPLICANT_1_SOLICITOR_AGREE_TO_RECEIVE_EMAILS, "applicant1SolicitorRepresented=\"Yes\"");
    }

    private void addOtherCourtCases(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant1OtherProceedings-Heading", null, "#### ${labelContentTheApplicantOrApplicant1UC}'s other proceedings:")
            .field(APPLICANT_1_LEGAL_PROCEEDINGS)
            .field(APPLICANT_1_LEGAL_PROCEEDINGS_DETAILS,
                "applicant1LegalProceedings=\"Yes\"")
            .field(FINANCIAL_ORDER)
            .field(APPLICANT_1_FINANCIAL_ORDERS_FOR,
                "applicant1FinancialOrder=\"Yes\"");
    }

    private void addApplicant2PersonalDetails(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant2-Heading", null, "### ${labelContentTheApplicant2UC}")
            .field(APPLICANT_2_FIRST_NAME)
            .field(APPLICANT_2_MIDDLE_NAME)
            .field(APPLICANT_2_LAST_NAME)
            .field(APPLICANT_2_GENDER)
            .field(APPLICANT_2_NAME_DIFFERENT_TO_MARRIAGE_CERTIFICATE)
            .field(
                APPLICANT_2_NAME_DIFFERENT_TO_MARRIAGE_CERTIFICATE_METHOD,
                andShowCondition(APP2_NAME_IS_DIFFERENT, APP2_NAME_CHANGE_EXPLANATION_REQUIRED)
            )
            .field(
                APPLICANT_2_NAME_DIFFERENT_TO_MARRIAGE_CERTIFICATE_OTHER_DETAILS,
                andShowCondition(APP2_NAME_IS_DIFFERENT, APP2_NAME_CHANGE_EXPLANATION_REQUIRED, APP2_HAS_CHANGED_NAME_IN_OTHER_WAY)
            )
            .field(APPLICANT_2_WHY_NAME_DIFFERENT, APP2_NAME_IS_DIFFERENT)
            .field(
                APPLICANT_2_WHY_NAME_DIFFERENT_OTHER_DETAILS,
                andShowCondition(APP2_NAME_IS_DIFFERENT, APP2_NAME_IS_DIFFERENT_FOR_OTHER_REASON)
            )
            .field(APPLICANT_2_LAST_NAME_CHANGED_WHEN_MARRIED)
            .field(APPLICANT_2_LAST_NAME_CHANGED_WHEN_MARRIED_METHOD)
            .field(APPLICANT_2_LAST_NAME_CHANGED_WHEN_MARRIED_OTHER_DETAILS)
            .field(APPLICANT_2_NAME_CHANGED_HOW)
            .field(APPLICANT_2_NAME_CHANGED_HOW_OTHER_DETAILS)
            .field(APPLICANT_2_CONTACT_DETAILS_TYPE, NEVER_SHOW)
            .field(APPLICANT_2_SCREEN_HAS_MARRIAGE_BROKEN, JOINT_APPLICATION)
            .field(APPLICANT_2_PCQ_ID)
            .field(APPLICANT_2_OFFLINE);
    }

    private void addApplicant2ContactDetails(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicant2DetailsAreConfidential-Heading",
                "applicant2ContactDetailsType=\"private\"",
                "#### ${labelContentTheApplicant2UC}'s contact details are confidential")
            .field(APPLICANT_2_PHONE_NUMBER, APPLICANT_2_CONTACT_DETAILS_PUBLIC)
            .field(APPLICANT_2_EMAIL, APPLICANT_2_CONTACT_DETAILS_PUBLIC)
            .field(APPLICANT_2_ADDRESS, APPLICANT_2_CONTACT_DETAILS_PUBLIC)
            .field(APPLICANT_2_ADDRESS_OVERSEAS, APPLICANT_2_CONTACT_DETAILS_PUBLIC_OVERSEAS);
    }

    private void addApplicant2Representation(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field(APPLICANT_2_AGREED_TO_RECEIVE_EMAILS)
            .field(APPLICANT_2_CANNOT_UPLOAD)
            .field(APPLICANT_2_CANNOT_UPLOAD_SUPPORTING_DOCUMENT)
            .field(APPLICANT_1_IS_APPLICANT_2_REPRESENTED, "applicant2SolicitorRepresented!=\"*\"")
            .field(APPLICANT_2_SOLICITOR_REPRESENTED)

            //Applicant 2 Solicitor
            .label("LabelApplicant2sSolicitorNewCases-Heading",
                "applicant2SolicitorRepresented=\"Yes\"",
                "#### ${labelContentApplicant2UC}'s solicitor details")
            .label("LabelApplicant2sSolicitorOldCases-Heading",
                "applicant1IsApplicant2Represented=\"Yes\" AND applicant2SolicitorRepresented!=\"*\"",
                "#### ${labelContentApplicant2UC}'s solicitor details")
            .field(APPLICANT_2_SOLICITOR_REFERENCE, "applicant2SolicitorRepresented!=\"No\"")
            .field(APPLICANT_2_SOLICITOR_NAME, "applicant2SolicitorRepresented!=\"No\"")
            .field(APPLICANT_2_SOLICITOR_ADDRESS, "applicant2SolicitorRepresented!=\"No\"")
            .field(APPLICANT_2_SOLICITOR_ADDRESS_OVERSEAS, APPLICANT_2_REPRESENTED_OVERSEAS)
            .field(APPLICANT_2_SOLICITOR_PHONE, "applicant2SolicitorRepresented!=\"No\"")
            .field(APPLICANT_2_SOLICITOR_EMAIL, "applicant2SolicitorRepresented!=\"No\"")
            .field(APPLICANT_2_SOLICITOR_FIRM_NAME, "applicant2SolicitorRepresented!=\"No\"")
            .field(APPLICANT_2_ORGANISATION_POLICY,
                "applicant2SolicitorRepresented!=\"No\" AND applicant2SolicitorOrganisationPolicy.Organisation.OrganisationID=\"*\""
            )
            .field(APPLICANT_2_SOLICITOR_AGREE_TO_RECEIVE_EMAILS_CHECKBOX, "applicant2SolicitorRepresented!=\"No\"")

            //Applicant 2 Other proceedings
            .label("LabelApplicant2OtherProceedings-Heading",
                JOINT_APPLICATION,
                "#### Applicant 2's other proceedings:")
            .field(APPLICANT_2_LEGAL_PROCEEDINGS, JOINT_APPLICATION)
            .field(APPLICANT_2_LEGAL_PROCEEDINGS_DETAILS,
                "applicant2LegalProceedings=\"Yes\" AND applicationType=\"jointApplication\"")
            .field(APPLICANT_2_FINANCIAL_ORDER, JOINT_APPLICATION)
            .field(APPLICANT_2_FINANCIAL_ORDERS_FOR,
                "applicant2FinancialOrder=\"Yes\" AND applicationType=\"jointApplication\"")
            .field(APPLICANT_2_STATEMENT_OF_TRUTH, JOINT_APPLICATION);
    }

    private void addMarriageAndCertificate(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelMarriage-Heading",
                "divorceOrDissolution = \"divorce\"", "### Marriage and certificate")
            .label("LabelCivilPartnership-Heading",
                "divorceOrDissolution = \"dissolution\"",
                "### Civil partnership and certificate")
            .field(MARRIAGE_DATE)
            .field(MARRIAGE_APPLICANT_1_NAME)
            .field(MARRIAGE_APPLICANT_2_NAME)
            .field(MARRIAGE_MARRIED_IN_UK)
            .field(MARRIAGE_PLACE_OF_MARRIAGE,
                "marriageMarriedInUk=\"No\" OR marriagePlaceOfMarriage=\"*\"")
            .field(MARRIAGE_COUNTRY_OF_MARRIAGE,
                "marriageMarriedInUk=\"No\" OR marriageCountryOfMarriage=\"*\"")
            .field(MARRIAGE_CERTIFICATE_IN_ENGLISH)
            .field(MARRIAGE_CERTIFIED_TRANSLATION, "marriageCertificateInEnglish=\"No\"");
    }

    private void addLegalConnections(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelJurisdiction-Heading", null, "### Jurisdiction")
            .field(JURISDICTION_CONNECTIONS);
    }

    private void addOtherProceedings(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field(URGENT_CASE)
            .field(SOL_URGENT_CASE_SUPPORTING_INFORMATION, "solUrgentCase=\"Yes\"")
            .field(SOL_STATEMENT_OF_RECONCILIATION_CERTIFY)
            .field(SOL_STATEMENT_OF_RECONCILIATION_DISCUSSED)
            .field(SOL_SIGN_STATEMENT_OF_TRUTH, "applicant1SolicitorRepresented=\"Yes\"")
            .field(SOL_STATEMENT_OF_RECONCILIATION_NAME)
            .field(SOL_STATEMENT_OF_RECONCILIATION_FIRM)
            .field(STATEMENT_OF_RECONCILIATION_COMMENTS);
    }

    private void addService(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("Label-SolicitorService", "serviceMethod=\"solicitorService\"", "### Solicitor Service")
            .field(SERVICE_METHOD, SOLE_APPLICATION)
            .field(SOL_SERVICE_FIRST_ATTEMPT_TO_SERVE, "serviceMethod=\"solicitorService\"")
            .field(SOL_SERVICE_DOCUMENTS_PREVIOUSLY_RETURNED, "serviceMethod=\"solicitorService\"")
            .field(SOL_SERVICE_DETAILS_OF_PREVIOUS_SERVICE, "serviceMethod=\"solicitorService\"")
            .field(SOL_SERVICE_DATE_PREVIOUS_SERVICE_RETURNED, "serviceMethod=\"solicitorService\"")
            .field(SOL_SERVICE_DATE_OF_SERVICE, "serviceMethod=\"solicitorService\"")
            .field(SOL_SERVICE_DOCUMENTS_SERVED, "serviceMethod=\"solicitorService\"")
            .field(SOL_SERVICE_ON_WHOM_SERVED, "serviceMethod=\"solicitorService\"")
            .field(SOL_SERVICE_HOW_SERVED, "serviceMethod=\"solicitorService\"")
            .field(SOL_SERVICE_SERVICE_PROCESSED_BY_PROCESS_SERVER,
                "serviceMethod=\"solicitorService\" AND solServiceServiceProcessedByProcessServer=\"*\"")
            .field(SOL_SERVICE_SERVICE_DETAILS,
                "solServiceHowServed=\"deliveredTo\" OR solServiceHowServed=\"postedTo\"")
            .field(SOL_SERVICE_ADDRESS_SERVED, "serviceMethod=\"solicitorService\"")
            .field(SOL_SERVICE_BEING_THE, "serviceMethod=\"solicitorService\"")
            .field(SOL_SERVICE_LOCATION_SERVED, "serviceMethod=\"solicitorService\"")
            .field(SOL_SERVICE_SPECIFY_LOCATION_SERVED,
                "serviceMethod=\"solicitorService\" AND solServiceLocationServed=\"otherSpecify\"")
            .field(SOL_SERVICE_SERVICE_SOT_NAME, "serviceMethod=\"solicitorService\"")
            .field(SOL_SERVICE_TRUTH_STATEMENT, "serviceMethod=\"solicitorService\" AND solServiceHowServed=\"*\"")
            .field(SOL_SERVICE_SERVICE_SOT_FIRM, "serviceMethod=\"solicitorService\"");
    }

    private void addApplicant1StatementOfTruth(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .field(APPLICANT_1_STATEMENT_OF_TRUTH);
    }

    private void addWithdrawApplicationDetails(final Tab.TabBuilder<CaseData, UserRole> tabBuilder) {
        tabBuilder
            .label("LabelApplicationWithdrawn-Heading",
                "cwWithdrawApplicationReason=\"*\" OR withdrawApplicationReason=\"*\" "
                    + "OR applicant2WithdrawApplicationReason=\"*\"", "#### Application Withdrawn")
            .field(WITHDRAW_APPLICATION_REASON,
                "withdrawApplicationReason=\"*\" AND " + showForState(Withdrawn, PendingRefund))
            .field(APPLICANT_2_WITHDRAW_APPLICATION_REASON,
                "applicant2WithdrawApplicationReason=\"*\" AND " + showForState(Withdrawn, PendingRefund))
            .field(CW_WITHDRAW_APPLICATION_REASON,
                "cwWithdrawApplicationReason=\"*\" AND " + showForState(Withdrawn))
            .field(CW_WITHDRAW_APPLICATION_DETAILS,
                "cwWithdrawApplicationDetails=\"*\" AND " + showForState(Withdrawn));
    }
}
