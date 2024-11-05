package uk.gov.hmcts.divorce.divorcecase.search;

public final class CaseFieldsConstants {

    public static final String APPLICANT_TYPE = "applicationType";
    public static final String APPLICANT_1_FIRST_NAME = "applicant1FirstName";
    public static final String APPLICANT_1_LAST_NAME = "applicant1LastName";
    public static final String APPLICANT_2_FIRST_NAME = "applicant2FirstName";
    public static final String APPLICANT_2_LAST_NAME = "applicant2LastName";
    public static final String APPLICANT_1_EMAIL = "applicant1Email";
    public static final String APPLICANT_2_EMAIL = "applicant2Email";
    public static final String FINANCIAL_ORDER = "applicant1FinancialOrder";
    public static final String APPLICANT_1_ORGANISATION_POLICY = "applicant1SolicitorOrganisationPolicy";
    public static final String MARRIAGE_DATE = "marriageDate";
    public static final String APPLICANT_1_HWF = "applicant1HWFReferenceNumber";
    public static final String APPLICANT_2_HWF = "applicant2HWFReferenceNumber";
    public static final String URGENT_CASE = "solUrgentCase";
    public static final String GENERAL_APPLICATION_URGENT_CASE = "generalApplicationUrgentCase";
    public static final String GENERAL_REFERRAL_URGENT_CASE = "generalReferralUrgentCase";
    public static final String GENERAL_REFERRAL_TYPE = "generalReferralType";
    public static final String FRAUD_REFERRAL_CASE = "generalReferralFraudCase";
    public static final String APPLICANT_1_FIRM_NAME = "applicant1SolicitorFirmName";
    public static final String EVIDENCE_HANDLED = "evidenceHandled";
    public static final String ALTERNATIVE_SERVICE_TYPE = "alternativeServiceType";
    public static final String APPLICANT_1_ADDRESS = "applicant1Address";
    public static final String APPLICANT_2_ADDRESS = "applicant2Address";
    public static final String CCD_REFERENCE = "[CASE_REFERENCE]";
    public static final String CASE_STATE = "[STATE]";
    public static final String DUE_DATE = "dueDate";
    public static final String LAST_MODIFIED_DATE = "[LAST_MODIFIED_DATE]";
    public static final String LAST_STATE_MODIFIED_DATE = "[LAST_STATE_MODIFIED_DATE]";
    public static final String SOL_PAYMENT_METHOD = "solPaymentHowToPay";
    public static final String APPLICANT_WELSH_TRANSLATION = "applicant1LanguagePreferenceWelsh";
    public static final String RESPONDENT_WELSH_TRANSLATION = "applicant2LanguagePreferenceWelsh";
    public static final String SCANNED_SUBTYPE_RECEIVED = "scannedSubtypeReceived";
    public static final String APPLICANT_2_SOL_APPLIED_FOR_FINAL_ORDER = "applicant2SolAppliedForFinalOrder";
    public static final String APPLICANT_2_APPLIED_FOR_FINAL_ORDER = "applicant2AppliedForFinalOrder";

    // required for Checkstyle
    private CaseFieldsConstants() {
    }
}
