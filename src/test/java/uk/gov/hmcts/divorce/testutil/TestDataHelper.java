package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import feign.Request;
import feign.Response;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.common.model.Gender;
import uk.gov.hmcts.divorce.common.model.Jurisdiction;
import uk.gov.hmcts.divorce.common.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.common.model.MarriageDetails;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
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
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.common.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.common.model.Gender.MALE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

public class TestDataHelper {

    public static final LocalDate LOCAL_DATE = LocalDate.of(2021, 04, 28);
    public static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 04, 28, 1, 0);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final TypeReference<HashMap<String, Object>> TYPE_REFERENCE = new TypeReference<>() {
    };

    private TestDataHelper() {

    }

    public static Applicant getApplicant() {
        return getApplicant(FEMALE);
    }

    public static Applicant getApplicant(Gender gender) {
        return Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .email(TEST_USER_EMAIL)
            .gender(gender)
            .build();
    }

    public static CaseData caseData() {
        return CaseData.builder()
            .applicant1(getApplicant())
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();
    }

    public static CaseData caseDataWithOrderSummary() {
        return CaseData
            .builder()
            .applicant1(getApplicant())
            .divorceOrDissolution(DIVORCE)
            .applicationFeeOrderSummary(OrderSummary.builder().paymentTotal("55000").build())
            .build();
    }

    public static CaseData validApplicant1CaseDataMap() {
        var marriageDetails = new MarriageDetails();
        marriageDetails.setDate(LocalDate.now().minus(1, YEARS).minus(1, DAYS));
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        var jurisdiction = new Jurisdiction();
        jurisdiction.setApplicant1Residence(YES);
        jurisdiction.setApplicant2Residence(YES);
        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_RESIDENT));

        var applicant1 = getApplicant();
        applicant1.setContactDetailsConfidential(ConfidentialAddress.KEEP);

        return CaseData
            .builder()
            .applicant1(applicant1)
            .applicant2(getApplicant(MALE))
            .financialOrder(NO)
            .helpWithFeesNeedHelp(NO)
            .prayerHasBeenGiven(YES)
            .statementOfTruth(YES)
            .marriageDetails(marriageDetails)
            .jurisdiction(jurisdiction)
            .build();

    }

    public static CaseData caseDataWithStatementOfTruth() {
        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();

        ListValue<Payment> payment = new ListValue<>(null, Payment
            .builder()
            .paymentAmount(55000)
            .paymentChannel("online")
            .paymentFeeId("FEE0001")
            .paymentReference("paymentRef")
            .paymentSiteId("AA04")
            .paymentStatus(PaymentStatus.SUCCESS)
            .paymentTransactionId("ge7po9h5bhbtbd466424src9tk")
            .build());

        return CaseData
            .builder()
            .applicant1(getApplicant())
            .divorceOrDissolution(DIVORCE)
            .divorceCostsClaim(YES)
            .solSignStatementOfTruth(YES)
            .applicant1SolicitorEmail(TEST_SOLICITOR_EMAIL)
            .applicationFeeOrderSummary(orderSummary)
            .payments(singletonList(payment))
            .build();
    }

    public static CallbackRequest callbackRequest() {
        return callbackRequest(caseDataWithOrderSummary());
    }

    public static CallbackRequest callbackRequest(CaseData caseData) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());

        return CallbackRequest
            .builder()
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
                    .id(TEST_CASE_ID)
                    .createdDate(LOCAL_DATE_TIME)
                    .build()
            )
            .build();
    }

    public static CallbackRequest callbackRequest(final CaseData caseData,
                                                  final String eventId) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());

        return CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
                    .id(TEST_CASE_ID)
                    .createdDate(LOCAL_DATE_TIME)
                    .build()
            )
            .build();
    }

    public static CallbackRequest callbackRequest(final CaseData caseData, String eventId, String state) {
        return CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
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
            "test-mini-draft-application.pdf",
            documentUrl + "/binary"
        );

        DivorceDocument divorceDocument = DivorceDocument
            .builder()
            .documentLink(ccdDocument)
            .documentFileName("test-mini-draft-application-12345.pdf")
            .documentType(documentType)
            .build();


        return ListValue
            .<DivorceDocument>builder()
            .id(DIVORCE_APPLICATION.getLabel())
            .value(divorceDocument)
            .build();
    }

    public static OrganisationPolicy<UserRole> organisationPolicy() {
        return OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation
                .builder()
                .organisationName("Test Organisation")
                .organisationId(TEST_ORG_ID)
                .build())
            .build();
    }

    public static OrganisationContactInformation organisationContactInformation() {
        return OrganisationContactInformation
            .builder()
            .addressLine1("addressLine1")
            .addressLine2("addressLine2")
            .addressLine3("addressLine3")
            .townCity("townCity")
            .country("country")
            .build();
    }

    public static String getFeeResponseAsJson() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(getFeeResponse());
    }
}
