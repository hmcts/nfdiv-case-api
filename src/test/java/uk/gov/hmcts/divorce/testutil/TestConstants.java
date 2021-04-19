package uk.gov.hmcts.divorce.testutil;

public final class TestConstants {
    public static final String TEST_USER_EMAIL = "test@test.com";
    public static final String TEST_FIRST_NAME = "test_first_name";
    public static final String TEST_LAST_NAME = "test_last_name";

    public static final String ABOUT_TO_START_URL = "/callbacks/about-to-start";
    public static final String ABOUT_TO_SUBMIT_URL = "/callbacks/about-to-submit";
    public static final String SUBMITTED_URL = "/callbacks/submitted";

    public static final String AUTH_HEADER_VALUE = "auth-header-value";
    public static final String INVALID_AUTH_TOKEN = "invalid_token";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String AUTHORIZATION = "Authorization";
    public static final String TEST_AUTHORIZATION_TOKEN = "test-auth";

    public static final String CCD_DATA = "ccd_data";
    public static final String FEE_CODE = "FEECODE1";
    public static final String ISSUE_FEE = "Issue Fee";

    public static final String PET_SOL_AUTH_TOKEN = "Bearer PetSolAuthToken";
    public static final String CASEWORKER_AUTH_TOKEN = "Bearer CaseWorkerAuthToken";
    public static final String SOLICITOR_USER_ID = "1";
    public static final String CASEWORKER_USER_ID = "2";

    public static final String TEST_SOL_USER_EMAIL = "testsol@test.com";
    public static final String TEST_CASEWORKER_USER_EMAIL = "testcw@test.com";
    public static final String TEST_CASEWORKER_USER_PASSWORD = "testpassword";

    public static final Long TEST_CASE_ID = 1616591401473378L;
    public static final String TEST_SERVICE_AUTH_TOKEN = "Bearer TestServiceAuth";

    public static final String ENGLISH_TEMPLATE_ID = "divorceminipetition";
    public static final String WELSH_TEMPLATE_ID = "FL-DIV-GNO-WEL-00256.docx";

    private TestConstants() {
    }
}
