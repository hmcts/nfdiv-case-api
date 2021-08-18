package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import feign.Request;
import feign.Response;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderDivorceParties;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudge;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
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
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ConfidentialAddress.SHARE;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_JOINT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_2_SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_2_SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
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
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
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

    public static Applicant applicantRepresentedBySolicitor() {
        final Applicant applicant = getApplicant(FEMALE);
        applicant.setSolicitor(Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        return applicant;
    }

    public static Applicant respondent() {
        return Applicant.builder()
            .firstName(APPLICANT_2_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .gender(MALE)
            .build();
    }

    public static Applicant respondentWithDigitalSolicitor() {
        final Applicant applicant = respondent();
        applicant.setSolicitor(Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .organisationPolicy(organisationPolicy())
            .build());
        return applicant;
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
            .applicationFeeOrderSummary(
                OrderSummary
                    .builder()
                    .paymentTotal("55000")
                    .fees(singletonList(getFeeListValue()))
                    .build()
            )
            .build();

        return CaseData
            .builder()
            .applicant1(getApplicant())
            .divorceOrDissolution(DIVORCE)
            .application(application)
            .build();
    }


    public static CaseData validJointApplicant1CaseData() {
        var marriageDetails = new MarriageDetails();
        marriageDetails.setDate(LocalDate.of(1990, 6, 10));
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

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
            .applicant1HelpWithFees(
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

    public static CaseData validApplicant1CaseData() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicant2(getApplicant2(MALE));
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplication().setApplicant1PrayerHasBeenGiven(YES);
        return caseData;
    }

    public static CaseData validApplicant2CaseData() {
        CaseData caseData = validApplicant1CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplication().setApplicant2HelpWithFees(HelpWithFees.builder()
            .needHelp(NO)
            .build());
        caseData.getApplication().setApplicant2PrayerHasBeenGiven(YES);
        caseData.getApplication().setApplicant2StatementOfTruth(YES);
        return caseData;
    }

    public static CaseData caseDataWithStatementOfTruth() {
        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();

        ListValue<Payment> payment = new ListValue<>(null, Payment
            .builder()
            .amount(55000)
            .channel("online")
            .feeCode("FEE0001")
            .reference("paymentRef")
            .status(PaymentStatus.SUCCESS)
            .transactionId("ge7po9h5bhbtbd466424src9tk")
            .build());

        var applicant1 = getApplicant();
        applicant1.setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build());

        var application = Application.builder()
            .solSignStatementOfTruth(YES)
            .applicationFeeOrderSummary(orderSummary)
            .applicationPayments(singletonList(payment))
            .build();

        return CaseData
            .builder()
            .applicant1(applicant1)
            .divorceOrDissolution(DIVORCE)
            .application(application)
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
        application.setApplicant1PrayerHasBeenGiven(YES);
        application.setApplicant1StatementOfTruth(YES);
        application.setJurisdiction(jurisdiction);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setFinancialOrder(NO);
        caseData.getApplicant1().setLegalProceedings(NO);

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
            .caseDetailsBefore(caseDetailsBefore(caseData))
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
                    .id(TEST_CASE_ID)
                    .createdDate(LOCAL_DATE_TIME)
                    .caseTypeId(CASE_TYPE)
                    .build()
            )
            .build();
    }

    public static CallbackRequest callbackRequest(final CaseData caseData, String eventId, String state) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        return CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(
                caseDetailsBefore(caseData))
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
                    .state(state)
                    .id(TEST_CASE_ID)
                    .caseTypeId(CASE_TYPE)
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

    public static ListValue<DivorceDocument> documentWithType(final DocumentType documentType) {
        return documentWithType(documentType, UUID.randomUUID().toString());
    }

    public static ListValue<DivorceDocument> documentWithType(final DocumentType documentType,
                                                              final String documentId) {
        String documentUrl = "http://localhost:8080/" + documentId;

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
                .organisationName(TEST_ORG_NAME)
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

    public static GeneralOrder getGeneralOrder(Document ccdDocument) {
        return GeneralOrder
            .builder()
            .generalOrderDate(LocalDate.of(2021, 1, 1))
            .generalOrderDetails("some details")
            .generalOrderDivorceParties(Set.of(GeneralOrderDivorceParties.RESPONDENT))
            .generalOrderJudgeType(GeneralOrderJudge.RECORDER)
            .generalOrderRecitals("test recitals")
            .generalOrderDraft(ccdDocument)
            .generalOrderJudgeName("some name")
            .generalOrderLegalAdvisorName("legal name")
            .build();
    }

    public static GeneralOrder getGeneralOrder() {
        return getGeneralOrder(null);
    }

    public static ListValue<DivorceGeneralOrder> getDivorceGeneralOrderListValue(Document ccdDocument, String listValueId) {
        DivorceDocument generalOrderDocument = DivorceDocument
            .builder()
            .documentFileName(ccdDocument.getFilename())
            .documentType(DocumentType.GENERAL_ORDER)
            .documentLink(ccdDocument)
            .build();

        DivorceGeneralOrder divorceGeneralOrder = DivorceGeneralOrder
            .builder()
            .generalOrderDocument(generalOrderDocument)
            .generalOrderDivorceParties(Set.of(GeneralOrderDivorceParties.RESPONDENT))
            .build();

        return ListValue
            .<DivorceGeneralOrder>builder()
            .id(listValueId)
            .value(divorceGeneralOrder)
            .build();
    }

    public static ListValue<DivorceDocument> getDivorceDocumentListValue(
        String url,
        String filename,
        DocumentType documentType
    ) {
        return ListValue.<DivorceDocument>builder()
            .id(UUID.randomUUID().toString())
            .value(DivorceDocument.builder()
                .documentType(documentType)
                .documentLink(Document
                    .builder()
                    .url(url)
                    .filename(filename)
                    .binaryUrl(url + "/binary")
                    .build()
                )
                .build())
            .build();
    }

    public static DynamicList getPbaNumbersForAccount(String accountNumber) {
        return DynamicList
            .builder()
            .value(
                DynamicListElement
                    .builder()
                    .code(UUID.randomUUID())
                    .label(accountNumber)
                    .build())
            .build();
    }

    private static CaseDetails caseDetailsBefore(CaseData caseData) {
        return CaseDetails
            .builder()
            .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
            .id(TEST_CASE_ID)
            .caseTypeId(CASE_TYPE)
            .build();
    }

    private static ListValue<Fee> getFeeListValue() {
        return ListValue
            .<Fee>builder()
            .value(Fee
                .builder()
                .amount("550")
                .description("fees for divorce")
                .code("FEE002")
                .build()
            )
            .build();
    }
}
