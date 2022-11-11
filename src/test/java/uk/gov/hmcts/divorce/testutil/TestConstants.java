package uk.gov.hmcts.divorce.testutil;

import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;

public final class TestConstants {
    public static final String TEST_APPLICANT_2_USER_EMAIL = "applicant2@test.com";
    public static final String TEST_USER_EMAIL = "test@test.com";
    public static final String TEST_SOLICITOR_EMAIL = "solicitor@test.com";
    public static final String TEST_SOLICITOR_NAME = "The Solicitor";
    public static final String TEST_SOLICITOR_ADDRESS = "The Solicitor's Address";
    public static final String TEST_SOLICITOR_FIRM_NAME = "Solicitor Bros";
    public static final String TEST_FIRST_NAME = "test_first_name";
    public static final String TEST_MIDDLE_NAME = "test_middle_name";
    public static final String TEST_LAST_NAME = "test_last_name";
    public static final String TEST_APP2_FIRST_NAME = "test_app2_first_name";
    public static final String TEST_APP2_MIDDLE_NAME = "test_app2_middle_name";
    public static final String TEST_APP2_LAST_NAME = "test_app2_last_name";
    public static final String TEST_REFERENCE = "test_ref";

    public static final String APPLICANT_2_FIRST_NAME = "applicant_2_first_name";
    public static final String APPLICANT_2_LAST_NAME = "applicant_2last_name";

    public static final String ABOUT_TO_START_URL = "/callbacks/about-to-start";
    public static final String ABOUT_TO_SUBMIT_URL = "/callbacks/about-to-submit";
    public static final String SUBMITTED_URL = "/callbacks/submitted";
    public static final String ABOUT_THE_SOL_MID_EVENT_URL = "/callbacks/mid-event?page=SolAboutTheSolicitor";
    public static final String SOL_PAYMENT_MID_EVENT_URL = "/callbacks/mid-event?page=SolPayment";
    public static final String SOLICITOR_UPDATE_CONTACT_MID_EVENT_URL = "/callbacks/mid-event?page=SolUpdateContactDetails";
    public static final String CREATE_GENERAL_ORDER_MID_EVENT_URL = "/callbacks/mid-event?page=CreateGeneralOrder";
    public static final String CO_REFUSAL_ORDER_WITH_MORE_INFO_MID_EVENT_URL = "/callbacks/mid-event?page=refusalOrderClarification";
    public static final String CO_REFUSAL_ORDER_WITH_AMENDMENTS_MID_EVENT_URL = "/callbacks/mid-event?page=amendApplication";

    public static final String AUTH_HEADER_VALUE = "auth-header-value";
    public static final String INVALID_AUTH_TOKEN = "invalid_token";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String AUTHORIZATION = "Authorization";
    public static final String TEST_AUTHORIZATION_TOKEN = "test-auth";
    public static final String TEST_SYSTEM_AUTHORISATION_TOKEN = "test-system-auth";

    public static final String CCD_DATA = "ccd_data";
    public static final String FEE_CODE = "FEECODE1";
    public static final String ISSUE_FEE = "Issue Fee";

    public static final String APP_1_SOL_AUTH_TOKEN = "Bearer App1SolAuthToken";
    public static final String CASEWORKER_AUTH_TOKEN = "Bearer CaseWorkerAuthToken";
    public static final String SYSTEM_UPDATE_AUTH_TOKEN = "Bearer SystemUpdateAuthToken";
    public static final String SOLICITOR_USER_ID = "1";
    public static final String CASEWORKER_USER_ID = "2";
    public static final String APP_2_CITIZEN_USER_ID = "3";
    public static final String SYSTEM_USER_USER_ID = "4";
    public static final String SUPER_USER_USER_ID = "5";

    public static final String TEST_SOL_USER_EMAIL = "testsol@test.com";
    public static final String TEST_APPLICANT_2_EMAIL = "testsol@test.com";
    public static final String TEST_CASEWORKER_USER_EMAIL = "testcw@test.com";
    public static final String TEST_SYSTEM_UPDATE_USER_EMAIL = "testsystem@test.com";
    public static final String TEST_SYSTEM_USER_PASSWORD = "testpassword";

    public static final Long TEST_CASE_ID = 1616591401473378L;
    public static final String FORMATTED_TEST_CASE_ID = "1616-5914-0147-3378";
    public static final String TEST_SERVICE_AUTH_TOKEN = "Bearer TestServiceAuth";

    public static final String ENGLISH_TEMPLATE_ID = "divorceminiapplication";
    public static final String WELSH_TEMPLATE_ID = "FL-DIV-GNO-WEL-00256.docx";
    public static final String BEARER = "Bearer ";
    public static final String TEST_ORG_ID = "ABC123";
    public static final String TEST_ORG_NAME = "Test Organisation";
    public static final String PROFESSIONAL_USERS_SIGN_IN_URL = "professional-sign-in-url/1234567890123456";

    public static final String SOLICITOR_MID_EVENT_RESPONSE = "classpath:solicitor-mid-event-response.json";
    public static final String SOLICITOR_MID_EVENT_ERROR = "classpath:solicitor-mid-event-error-response.json";
    public static final String SOLICITOR_MID_EVENT_EMAIL_ERROR = "classpath:solicitor-mid-event-email-error-response.json";
    public static final String LINE_1_LINE_2_CITY_POSTCODE = "line1\nline2\ncity\npostcode";
    public static final AddressGlobalUK APPLICANT_ADDRESS = AddressGlobalUK.builder()
        .addressLine1("line1")
        .addressLine2("line2")
        .postTown("city")
        .postCode("postcode")
        .build();
    public static final String ACCESS_CODE = "6E69DKFX";

    public static final String SIGN_IN_DIVORCE_TEST_URL = "divorceTestUrl";
    public static final String APPLICANT_2_SIGN_IN_DIVORCE_TEST_URL = "applicant2DivorceTestUrl";
    public static final String SIGN_IN_DISSOLUTION_TEST_URL = "dissolutionTestUrl";
    public static final String APPLICANT_2_SIGN_IN_DISSOLUTION_TEST_URL = "applicant2DissolutionTestUrl";

    public static final String EMPTY_STRING = "";

    public static final String TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID = "FL-NFD-GOR-ENG-Application-Sole_V6.docx";
    public static final String TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID = "FL-NFD-APP-ENG-Divorce-Application-Joint.docx";

    public static final String TEST_FINANCIAL_ORDER_POLICY_HEADER_TEXT = "The applicant wants to apply for a financial order.";
    public static final String TEST_FINANCIAL_ORDER_POLICY_HINT_TEXT = "A financial order is a legal document that describes how the "
        + "money and property will be divided. The application will be done separately.";

    private TestConstants() {
    }
}
