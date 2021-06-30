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
import uk.gov.hmcts.divorce.common.model.Application;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.CaseInvite;
import uk.gov.hmcts.divorce.common.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.common.model.Gender;
import uk.gov.hmcts.divorce.common.model.HelpWithFees;
import uk.gov.hmcts.divorce.common.model.Jurisdiction;
import uk.gov.hmcts.divorce.common.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.common.model.MarriageDetails;
import uk.gov.hmcts.divorce.common.model.Solicitor;
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
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.common.model.ConfidentialAddress.SHARE;
import static uk.gov.hmcts.divorce.common.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.common.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.common.model.Gender.MALE;
import static uk.gov.hmcts.divorce.common.model.JurisdictionConnections.APP_1_RESIDENT_JOINT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_2_SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_2_SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_SIGN_IN_DISSOLUTION_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DISSOLUTION_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DIVORCE_TEST_URL;
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
            .languagePreferenceWelsh(NO)
            .contactDetailsConfidential(SHARE)
            .build();
    }

    public static Applicant getApplicant2(Gender gender) {
        return Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .gender(gender)
            .build();
    }

    public static Applicant getJointApplicant2(Gender gender) {
        return Applicant.builder()
            .gender(gender)
            .build();
    }

    public static Applicant getInvalidApplicant() {
        return Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .email(TEST_USER_EMAIL)
            .build();
    }

    public static CaseData caseData() {
        return CaseData.builder()
            .applicant1(getApplicant())
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .caseInvite(new CaseInvite())
            .build();
    }

    public static CaseData invalidCaseData() {
        return CaseData.builder()
            .applicant1(getInvalidApplicant())
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();
    }

    public static CaseData caseDataWithOrderSummary() {
        var application = Application.builder()
            .applicationFeeOrderSummary(OrderSummary.builder().paymentTotal("55000").build())
            .build();

        return CaseData
            .builder()
            .applicant1(getApplicant())
            .divorceOrDissolution(DIVORCE)
            .application(application)
            .build();
    }

    public static CaseData validJointApplicant1CaseDataMap() {
        var marriageDetails = new MarriageDetails();
        marriageDetails.setDate(LocalDate.of(1990, 6, 10));
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        var jurisdiction = new Jurisdiction();
        jurisdiction.setApplicant1Residence(YES);
        jurisdiction.setApplicant2Residence(YES);
        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_RESIDENT));

        var applicant1 = getApplicant();
        applicant1.setContactDetailsConfidential(ConfidentialAddress.KEEP);
        applicant1.setFinancialOrder(NO);

        var application = Application.builder()
            .marriageDetails(marriageDetails)
            .jurisdiction(jurisdiction)
            .helpWithFees(
                HelpWithFees.builder()
                    .needHelp(NO)
                    .build()
            )
            .build();

        return CaseData
            .builder()
            .applicant1(applicant1)
            .applicant2(getJointApplicant2(MALE))
            .caseInvite(CaseInvite.builder().applicant2InviteEmailAddress(TEST_USER_EMAIL).build())
            .divorceOrDissolution(DIVORCE)
            .application(application)
            .build();
    }

    public static CaseData validApplicant1CaseDataMap() {
        CaseData caseData = validJointApplicant1CaseDataMap();
        caseData.setApplicant2(getApplicant2(MALE));
        caseData.getApplication().setStatementOfTruth(YES);
        caseData.getApplication().setPrayerHasBeenGiven(YES);
        return caseData;
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

        var applicant1 = getApplicant();
        applicant1.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build());

        var application = Application.builder()
            .divorceCostsClaim(YES)
            .solSignStatementOfTruth(YES)
            .applicationFeeOrderSummary(orderSummary)
            .build();

        return CaseData
            .builder()
            .applicant1(applicant1)
            .divorceOrDissolution(DIVORCE)
            .application(application)
            .payments(singletonList(payment))
            .build();
    }

    public static CaseData validCaseDataForIssueApplication() {
        final MarriageDetails marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(format("%s %s", TEST_FIRST_NAME, TEST_LAST_NAME));
        marriageDetails.setApplicant2Name(format("%s %s", TEST_FIRST_NAME, TEST_LAST_NAME));
        marriageDetails.setDate(LocalDate.of(1990, 6, 10));
        marriageDetails.setPlaceOfMarriage("Somewhere");

        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setConnections(Set.of(APP_1_RESIDENT_JOINT));
        jurisdiction.setApplicant1Residence(YES);
        jurisdiction.setApplicant2Residence(YES);

        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.setApplicant2(getApplicant());

        final Application application = caseData.getApplication();
        application.setDocumentUploadComplete(YES);
        application.setMarriageDetails(marriageDetails);
        application.setPrayerHasBeenGiven(YES);
        application.setStatementOfTruth(YES);
        application.setJurisdiction(jurisdiction);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setFinancialOrder(NO);
        caseData.getApplication().setLegalProceedings(NO);

        return caseData;
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

    public static Map<String, String> getConfigTemplateVars() {
        return Map.of(
            SIGN_IN_DIVORCE_URL, SIGN_IN_DIVORCE_TEST_URL,
            SIGN_IN_DISSOLUTION_URL, SIGN_IN_DISSOLUTION_TEST_URL,
            APPLICANT_2_SIGN_IN_DIVORCE_URL, APPLICANT_2_SIGN_IN_DIVORCE_TEST_URL,
            APPLICANT_2_SIGN_IN_DISSOLUTION_URL, APPLICANT_2_SIGN_IN_DISSOLUTION_TEST_URL
        );
    }
}
