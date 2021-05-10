package uk.gov.hmcts.divorce.testutil;

import feign.FeignException;
import feign.Request;
import feign.Response;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.common.model.Gender;
import uk.gov.hmcts.divorce.common.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.ccd.sdk.type.Fee.getValueInPence;
import static uk.gov.hmcts.divorce.common.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

public class TestDataHelper {

    public static final LocalDate LOCAL_DATE = LocalDate.of(2021, 04, 28);
    public static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 04, 28, 1, 0);

    private TestDataHelper() {

    }

    public static CaseData caseData() {
        return CaseData.builder()
            .petitionerFirstName(TEST_FIRST_NAME)
            .petitionerMiddleName(TEST_MIDDLE_NAME)
            .petitionerLastName(TEST_LAST_NAME)
            .petitionerEmail(TEST_USER_EMAIL)
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();
    }

    public static Map<String, Object> caseDataMap() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("petitionerFirstName", TEST_FIRST_NAME);
        caseDataMap.put("petitionerMiddleName", TEST_MIDDLE_NAME);
        caseDataMap.put("petitionerLastName", TEST_LAST_NAME);
        caseDataMap.put("divorceOrDissolution", DIVORCE);
        caseDataMap.put("petitionerEmail", TEST_USER_EMAIL);
        return caseDataMap;
    }

    public static Map<String, Object> validPetitionerCaseDataMap() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("petitionerFirstName", TEST_FIRST_NAME);
        caseDataMap.put("petitionerMiddleName", TEST_MIDDLE_NAME);
        caseDataMap.put("petitionerLastName", TEST_LAST_NAME);
        caseDataMap.put("respondentFirstName", TEST_FIRST_NAME);
        caseDataMap.put("respondentLastName", TEST_LAST_NAME);
        caseDataMap.put("financialOrder", YesOrNo.NO);
        caseDataMap.put("inferredPetitionerGender", Gender.FEMALE);
        caseDataMap.put("inferredRespondentGender", Gender.MALE);
        caseDataMap.put("marriagePetitionerName", TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        caseDataMap.put("petitionerContactDetailsConfidential", ConfidentialAddress.KEEP);
        caseDataMap.put("prayerHasBeenGiven", YesOrNo.YES);
        caseDataMap.put("statementOfTruth", YesOrNo.YES);
        caseDataMap.put("marriageDate", LocalDate.now().minus(1, YEARS).minus(1, DAYS));
        caseDataMap.put("jurisdictionPetitionerResidence", YesOrNo.YES);
        caseDataMap.put("jurisdictionConnections", Set.of(JurisdictionConnections.PET_RESP_RESIDENT));
        return caseDataMap;
    }

    public static CallbackRequest callbackRequest() {
        return callbackRequest(caseDataMap());
    }

    public static CallbackRequest callbackRequest(final Map<String, Object> caseData) {
        return CallbackRequest
            .builder()
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(caseData)
                    .id(TEST_CASE_ID)
                    .createdDate(LOCAL_DATE_TIME)
                    .build()
            )
            .build();
    }

    public static CallbackRequest callbackRequest(final Map<String, Object> caseData,
                                                  final String eventId) {
        return CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(caseData)
                    .id(TEST_CASE_ID)
                    .createdDate(LOCAL_DATE_TIME)
                    .build()
            )
            .build();
    }

    public static CallbackRequest callbackRequest(final Map<String, Object> caseData, String eventId, String state) {
        return CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(caseData)
                    .state(state)
                    .id(TEST_CASE_ID)
                    .build()
            )
            .build();
    }

    public static FeeResponse getFeeResponse() {
        return FeeResponse
            .builder()
            .feeCode(FEE_CODE)
            .amount(10.0)
            .description(ISSUE_FEE)
            .version(1)
            .build();
    }

    public static OrderSummary getDefaultOrderSummary() {
        return OrderSummary
            .builder()
            .fees(singletonList(getDefaultFeeItem()))
            .build();
    }

    public static ListValue<Fee> getFeeItem(double feeAmount, String feeCode, String description, int version) {
        return ListValue
            .<Fee>builder()
            .value(
                Fee
                    .builder()
                    .amount(getValueInPence(feeAmount))
                    .code(feeCode)
                    .description(description)
                    .version(String.valueOf(version))
                    .build()
            )
            .build();
    }

    public static ListValue<Fee> getDefaultFeeItem() {
        return getFeeItem(10.50, FEE_CODE, "Issue Petition Fee", 1);
    }

    public static FeignException feignException(int status, String reason) {
        byte[] emptyBody = {};
        Request request = Request.create(GET, EMPTY, Map.of(), emptyBody, UTF_8, null);

        return FeignException.errorStatus(
            "idamRequestFailed",
            Response.builder()
                .request(request)
                .status(status)
                .headers(Collections.emptyMap())
                .reason(reason)
                .build()
        );
    }

    public static ListValue<DivorceDocument> documentWithType(DocumentType documentType) {
        String documentUrl = "http://localhost:8080/" + UUID.randomUUID();

        Document ccdDocument = new Document(
            documentUrl,
            "test-mini-draft-petition.pdf",
            documentUrl + "/binary"
        );

        DivorceDocument divorceDocument = DivorceDocument
            .builder()
            .documentLink(ccdDocument)
            .documentFileName("test-mini-draft-petition-12345.pdf")
            .documentType(documentType)
            .build();


        return ListValue
            .<DivorceDocument>builder()
            .id(DIVORCE_APPLICATION.getLabel())
            .value(divorceDocument)
            .build();
    }
}
