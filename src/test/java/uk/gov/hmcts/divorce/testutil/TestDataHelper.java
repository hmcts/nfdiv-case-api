package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import feign.Request;
import feign.Response;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.DocumentsServedBeingThe;
import uk.gov.hmcts.divorce.divorcecase.model.DocumentsServedHow;
import uk.gov.hmcts.divorce.divorcecase.model.DocumentsServedWhere;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderDivorceParties;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudge;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
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
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_JOINT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
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
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
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
            .keepContactDetailsConfidential(NO)
            .financialOrder(NO)
            .build();
    }

    public static Applicant getApplicantWithAddress() {
        return Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
            .email(TEST_USER_EMAIL)
            .gender(MALE)
            .languagePreferenceWelsh(NO)
            .homeAddress(AddressGlobalUK.builder()
                .addressLine1("line 1")
                .postTown("town")
                .postCode("postcode")
                .build())
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
        var caseData = validApplicant1CaseData();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummaryWithFee());

        return caseData;
    }

    public static CaseData jointCaseDataWithOrderSummary() {
        CaseData caseData = caseDataWithOrderSummary();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setCaseInvite(new CaseInvite());
        caseData.getCaseInvite().setApplicant2InviteEmailAddress(TEST_APPLICANT_2_USER_EMAIL);
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplication().setApplicant2StatementOfTruth(YES);
        caseData.getApplication().setApplicant2PrayerHasBeenGiven(YES);

        return caseData;
    }

    public static CaseData validJointApplicant1CaseData() {
        var marriageDetails = new MarriageDetails();
        marriageDetails.setDate(LocalDate.of(1990, 6, 10));
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setMarriedInUk(YES);

        var jurisdiction = new Jurisdiction();
        jurisdiction.setApplicant1Residence(YES);
        jurisdiction.setApplicant2Residence(YES);
        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_RESIDENT));

        var applicant1 = getApplicant();
        applicant1.setKeepContactDetailsConfidential(YES);
        applicant1.setFinancialOrder(NO);
        applicant1.setLegalProceedings(NO);

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
            .caseInvite(CaseInvite.builder().applicant2InviteEmailAddress(TEST_APPLICANT_2_USER_EMAIL).build())
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
        caseData.setApplicant2(getApplicantWithAddress());
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
            .applicant1PrayerHasBeenGiven(YES)
            .applicationFeeOrderSummary(orderSummary)
            .applicationPayments(singletonList(payment))
            .marriageDetails(getMarriageDetails())
            .jurisdiction(getJurisdiction())
            .build();

        return CaseData
            .builder()
            .applicant1(applicant1)
            .applicant2(getApplicant2(FEMALE))
            .divorceOrDissolution(DIVORCE)
            .application(application)
            .build();
    }

    public static MarriageDetails getMarriageDetails() {
        var marriageDetails = new MarriageDetails();
        marriageDetails.setDate(LocalDate.of(1990, 6, 10));
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);

        return marriageDetails;
    }

    public static Jurisdiction getJurisdiction() {
        final Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setConnections(Set.of(APP_1_RESIDENT_JOINT));
        jurisdiction.setApplicant1Residence(YES);
        jurisdiction.setApplicant2Residence(YES);

        return jurisdiction;
    }

    public static SolicitorService getSolicitorService() {
        final SolicitorService solicitorService = new SolicitorService();
        solicitorService.setDateOfService(LocalDate.now());
        solicitorService.setDocumentsServed("docsServed");
        solicitorService.setAddressServed("addressServed");
        solicitorService.setHowServed(DocumentsServedHow.COURT_PERMITTED);
        solicitorService.setLocationServed(DocumentsServedWhere.PLACE_BUSINESS);
        solicitorService.setBeingThe(DocumentsServedBeingThe.APPLICANT);
        solicitorService.setOnWhomServed("servedTo");
        solicitorService.setServiceSotName("solicitor name");
        solicitorService.setServiceSotFirm("solicitor firm");
        return solicitorService;
    }

    public static ConditionalOrder getConditionalOrder() {
        final ConditionalOrder conditionalOrder = new ConditionalOrder();
        conditionalOrder.setDateSubmitted(LocalDateTime.now());
        conditionalOrder.setApplyForConditionalOrder(YES);
        conditionalOrder.setChangeOrAddToApplication(NO);
        conditionalOrder.setIsEverythingInPetitionTrue(YES);
        conditionalOrder.setAddNewDocuments(NO);
        return conditionalOrder;
    }


    public static CaseData validCaseDataForIssueApplication() {
        final MarriageDetails marriageDetails = new MarriageDetails();
        marriageDetails.setApplicant1Name(format("%s %s", TEST_FIRST_NAME, TEST_LAST_NAME));
        marriageDetails.setApplicant2Name(format("%s %s", TEST_FIRST_NAME, TEST_LAST_NAME));
        marriageDetails.setDate(LocalDate.of(1990, 6, 10));
        marriageDetails.setPlaceOfMarriage("Somewhere");

        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.setApplicant2(getApplicantWithAddress());

        final Application application = caseData.getApplication();
        application.setDocumentUploadComplete(YES);
        application.setMarriageDetails(marriageDetails);
        application.setApplicant1PrayerHasBeenGiven(YES);
        application.setApplicant1StatementOfTruth(YES);
        application.setJurisdiction(getJurisdiction());
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setFinancialOrder(NO);
        caseData.getApplicant1().setLegalProceedings(NO);
        caseData.setCaseInvite(new CaseInvite());
        caseData.getCaseInvite().setApplicant2InviteEmailAddress(TEST_APPLICANT_2_USER_EMAIL);

        return caseData;
    }

    public static CaseData validCaseDataForAosSubmitted() {
        CaseData caseData = validCaseDataForIssueApplication();

        caseData.getApplicant2().setLegalProceedings(NO);
        caseData.getAcknowledgementOfService().setConfirmReadPetition(YES);
        caseData.getAcknowledgementOfService().setJurisdictionAgree(YES);
        caseData.getAcknowledgementOfService().setPrayerHasBeenGiven(YES);
        caseData.getAcknowledgementOfService().setStatementOfTruth(YES);

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

    public static CallbackRequest callbackRequest(final BulkActionCaseData caseData,
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
                    .caseTypeId(BulkActionCaseTypeConfig.CASE_TYPE)
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
            "test-draft-divorce-application.pdf",
            documentUrl + "/binary"
        );

        DivorceDocument divorceDocument = DivorceDocument
            .builder()
            .documentLink(ccdDocument)
            .documentFileName("test-draft-divorce-application-12345.pdf")
            .documentType(documentType)
            .build();


        return ListValue
            .<DivorceDocument>builder()
            .id(APPLICATION.getLabel())
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
                    .build()
            )
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

    private static CaseDetails caseDetailsBefore(BulkActionCaseData caseData) {
        return CaseDetails
            .builder()
            .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
            .id(TEST_CASE_ID)
            .caseTypeId(BulkActionCaseTypeConfig.CASE_TYPE)
            .build();
    }

    public static ListValue<Fee> getFeeListValue() {
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

    public static OrderSummary orderSummaryWithFee() {
        return OrderSummary
            .builder()
            .paymentTotal("55000")
            .fees(singletonList(getFeeListValue()))
            .build();
    }

    public static ListValue<BulkListCaseDetails> getBulkListCaseDetailsListValue(String caseReference) {
        final var bulkListCaseDetails = BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference(caseReference)
                .build())
            .build();

        return ListValue
            .<BulkListCaseDetails>builder()
            .value(bulkListCaseDetails)
            .build();
    }
}
