package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;
import feign.FeignException;
import feign.Request;
import feign.Response;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.KeyValue;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.DocumentsServedBeingThe;
import uk.gov.hmcts.divorce.divorcecase.model.DocumentsServedHow;
import uk.gov.hmcts.divorce.divorcecase.model.DocumentsServedWhere;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderDivorceParties;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrderJudgeOrLegalAdvisorType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.endpoint.data.FormType;
import uk.gov.hmcts.divorce.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.endpoint.model.input.InputScannedDoc;
import uk.gov.hmcts.divorce.endpoint.model.input.InputScannedDocUrl;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static feign.Request.HttpMethod.GET;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.FORM;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationRemindApplicant2Notification.APPLICANT_2_SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationRemindApplicant2Notification.APPLICANT_2_SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer.I_CONFIRM;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CIVIL_PARTNER_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.HUSBAND_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.WIFE_JOINT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
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

    public static final LocalDate LOCAL_DATE = LocalDate.of(2021, 4, 28);
    public static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2021, 4, 28, 1, 0);
    private static final String DOC_CONTROL_NUMBER = "61347040100200003";
    private static final LocalDateTime DOC_SCANNED_DATE_META_INFO = LocalDateTime.of(2022, 1, 1, 12, 12, 0);
    private static final String DOCUMENT_URL = "http://localhost:8080/documents/640055da-9330-11ec-b909-0242ac120002";
    private static final String DOCUMENT_BINARY_URL = "http://localhost:8080/documents/640055da-9330-11ec-b909-0242ac120002/binary";
    private static final String FILE_NAME = "61347040100200003.pdf";
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
            .contactDetailsType(PUBLIC)
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
            .address(AddressGlobalUK.builder()
                .addressLine1("line 1")
                .postTown("town")
                .postCode("postcode")
                .country("UK")
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
            .firstName(TEST_FIRST_NAME)
            .middleName(TEST_MIDDLE_NAME)
            .lastName(TEST_LAST_NAME)
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
        applicant.setSolicitorRepresented(YES);
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
        applicant.setSolicitorRepresented(YES);
        return applicant;
    }

    public static CaseData caseData() {
        return CaseData.builder()
            .applicant1(getApplicant())
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .caseInvite(new CaseInvite(null, null, null))
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
        caseData.setCaseInvite(new CaseInvite(TEST_APPLICANT_2_USER_EMAIL, null, null));
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplication().setApplicant2StatementOfTruth(YES);
        caseData.getApplication().setApplicant2PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));

        return caseData;
    }

    public static CaseData validJointApplicant1CaseData() {
        var marriageDetails = new MarriageDetails();
        marriageDetails.setDate(LocalDate.of(1990, 6, 10));
        marriageDetails.setApplicant1Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setApplicant2Name(TEST_FIRST_NAME + " " + TEST_LAST_NAME);
        marriageDetails.setMarriedInUk(YES);

        var applicant1 = getApplicant();
        applicant1.setContactDetailsType(PRIVATE);
        applicant1.setFinancialOrder(NO);
        applicant1.setLegalProceedings(NO);
        applicant1.setFirstName(TEST_FIRST_NAME);
        applicant1.setLastName(TEST_LAST_NAME);

        var application = Application.builder()
            .marriageDetails(marriageDetails)
            .jurisdiction(getJurisdiction())
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
            .applicationType(JOINT_APPLICATION)
            .build();
    }

    public static CaseData validApplicant1CaseData() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplicant2(getApplicant2(MALE));
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplication().setApplicant1PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
        return caseData;
    }

    public static CaseData validApplicant2CaseData() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplication().setApplicant1PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
        caseData.setApplicant2(getApplicantWithAddress());
        caseData.getApplication().setApplicant2HelpWithFees(HelpWithFees.builder()
            .needHelp(NO)
            .build());
        caseData.getApplication().setApplicant2PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
        caseData.getApplication().setApplicant2StatementOfTruth(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);

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
        applicant1.setSolicitorRepresented(YES);

        var application = Application.builder()
            .solSignStatementOfTruth(YES)
            .applicant1PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM))
            .applicationFeeOrderSummary(orderSummary)
            .applicationPayments(singletonList(payment))
            .marriageDetails(getMarriageDetails())
            .jurisdiction(getJurisdiction())
            .build();

        return CaseData
            .builder()
            .applicationType(SOLE_APPLICATION)
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
        jurisdiction.setConnections(Set.of(APP_1_APP_2_RESIDENT));
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

    public static ConditionalOrderQuestions getConditionalOrderQuestions() {
        final ConditionalOrderQuestions conditionalOrderQuestions = new ConditionalOrderQuestions();
        conditionalOrderQuestions.setSubmittedDate(LocalDateTime.now());
        conditionalOrderQuestions.setApplyForConditionalOrder(YES);
        conditionalOrderQuestions.setChangeOrAddToApplication(NO);
        conditionalOrderQuestions.setIsEverythingInApplicationTrue(YES);
        return conditionalOrderQuestions;
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
        application.setApplicant1PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
        application.setApplicant1StatementOfTruth(YES);
        application.setJurisdiction(getJurisdiction());
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setFinancialOrder(NO);
        caseData.getApplicant1().setLegalProceedings(NO);
        caseData.setCaseInvite(new CaseInvite(TEST_APPLICANT_2_USER_EMAIL, null, null));

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

    public static CaseData validCaseWithCourtHearing() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        final LocalDateTime testHearingDateAndTime = LocalDateTime.of(2021, 11, 8, 14, 56);
        final LocalDate testDecisionDate = LocalDate.of(2020, 3, 8);

        caseData.setConditionalOrder(ConditionalOrder.builder()
            .dateAndTimeOfHearing(testHearingDateAndTime)
            .court(BURY_ST_EDMUNDS)
            .decisionDate(testDecisionDate)
            .build());

        return caseData;
    }

    public static CaseData validCaseDataForAwaitingFinalOrder() {
        CaseData caseData = validCaseWithCourtHearing();
        LocalDateTime dateAndTimeOfHearing = caseData.getConditionalOrder().getDateAndTimeOfHearing();

        FinalOrder finalOrder = caseData.getFinalOrder();

        finalOrder.setDateFinalOrderEligibleFrom(caseData.getFinalOrder().getDateFinalOrderEligibleFrom(dateAndTimeOfHearing));
        finalOrder.setDateFinalOrderEligibleToRespondent(finalOrder.calculateDateFinalOrderEligibleToRespondent());

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

    public static CallbackRequest callbackRequestBeforeAndAfter(final CaseData caseDataBefore,
                                                                final CaseData caseData,
                                                                final String eventId) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        return CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(caseDetailsBefore(caseDataBefore))
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

    public static Map<String, String> getMainTemplateVars() {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_REFERENCE, "1234-5678-9012-3456");
        templateVars.put(IS_DIVORCE, CommonContent.YES);
        templateVars.put(IS_DISSOLUTION, CommonContent.NO);
        templateVars.put(FIRST_NAME, TEST_FIRST_NAME);
        templateVars.put(LAST_NAME, TEST_LAST_NAME);
        templateVars.put(PARTNER, "partner");
        templateVars.put(COURT_EMAIL, "courtEmail");
        return templateVars;
    }

    public static Map<String, String> getConditionalOrderTemplateVars(ApplicationType applicationType) {
        Map<String, String> templateVars = getMainTemplateVars();
        templateVars.put(JOINT_CONDITIONAL_ORDER, CommonContent.NO);
        templateVars.put(WIFE_JOINT, CommonContent.NO);
        templateVars.put(HUSBAND_JOINT, CommonContent.NO);
        templateVars.put(CIVIL_PARTNER_JOINT, CommonContent.NO);

        if (applicationType.equals(JOINT_APPLICATION)) {
            templateVars.put(JOINT_CONDITIONAL_ORDER, CommonContent.YES);
            templateVars.put(WIFE_JOINT, CommonContent.YES);
        }
        return templateVars;
    }

    public static Map<String, String> getBasicTemplateVars() {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(APPLICATION_REFERENCE, "1234-5678-9012-3456");
        templateVars.put(SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        templateVars.put(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME));
        templateVars.put(RESPONDENT_NAME, join(" ", APPLICANT_2_FIRST_NAME, APPLICANT_2_LAST_NAME));
        return templateVars;
    }

    public static GeneralOrder getGeneralOrder(Document ccdDocument) {
        return GeneralOrder
            .builder()
            .generalOrderDate(LocalDate.of(2021, 1, 1))
            .generalOrderDetails("some details")
            .generalOrderDivorceParties(Set.of(GeneralOrderDivorceParties.RESPONDENT))
            .generalOrderJudgeOrLegalAdvisorType(GeneralOrderJudgeOrLegalAdvisorType.DISTRICT_JUDGE)
            .generalOrderRecitals("test recitals")
            .generalOrderDraft(ccdDocument)
            .generalOrderJudgeOrLegalAdvisorName("some name")
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

    public static ListValue<CaseLink> getCaseLinkListValue(String caseReference) {
        final var caseLink = CaseLink.builder()
            .caseReference(caseReference)
            .build();

        return ListValue
            .<CaseLink>builder()
            .value(caseLink)
            .build();
    }

    public static OcrDataValidationRequest ocrDataValidationRequest() {
        return OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    KeyValue.builder()
                        .key("applicant1Name")
                        .value("bob")
                        .build())
            )
            .build();
    }

    public static List<KeyValue> populateCommonOcrDataFields() {
        List<KeyValue> kv = new ArrayList<>();
        kv.add(populateKeyValue("aSoleApplication", "true"));
        kv.add(populateKeyValue("marriageOrCivilPartnershipCertificate", "true"));
        kv.add(populateKeyValue("translation", "false"));
        kv.add(populateKeyValue("soleApplicantOrApplicant1FirstName", "bob"));
        kv.add(populateKeyValue("soleApplicantOrApplicant1LastName", "builder"));
        kv.add(populateKeyValue("respondentOrApplicant2FirstName", "the"));
        kv.add(populateKeyValue("respondentOrApplicant2LastName", "respondent"));
        kv.add(populateKeyValue("respondentOrApplicant2MarriedName", "No"));
        kv.add(populateKeyValue("serveOutOfUK", "Yes"));
        kv.add(populateKeyValue("respondentServePostOnly", "true"));
        kv.add(populateKeyValue("respondentDifferentServiceAddress", "No"));
        kv.add(populateKeyValue("marriageOutsideOfUK", "No"));
        kv.add(populateKeyValue("dateOfMarriageOrCivilPartnershipDay", "01"));
        kv.add(populateKeyValue("dateOfMarriageOrCivilPartnershipMonth", "01"));
        kv.add(populateKeyValue("dateOfMarriageOrCivilPartnershipYear", "1990"));
        kv.add(populateKeyValue("soleOrApplicant1FullNameAsOnCert", "bob builder"));
        kv.add(populateKeyValue("respondentOrApplicant2FullNameAsOnCert", "the respondent"));
        kv.add(populateKeyValue("detailsOnCertCorrect", "Yes"));
        kv.add(populateKeyValue("jurisdictionReasonsBothPartiesHabitual", "true"));
        kv.add(populateKeyValue("existingOrPreviousCourtCases", "No"));
        kv.add(populateKeyValue("soleOrApplicant1FinancialOrder", "No"));
        kv.add(populateKeyValue("soleOrApplicant1ConfirmationOfBreakdown", "true"));
        kv.add(populateKeyValue("soleApplicantOrApplicant1StatementOfTruth", "true"));
        kv.add(populateKeyValue("soleApplicantOrApplicant1LegalRepStatementOfTruth", "true"));
        kv.add(populateKeyValue("soleApplicantOrApplicant1OrLegalRepSignature", "signed"));
        kv.add(populateKeyValue("soleApplicantOrApplicant1Signing", "true"));
        kv.add(populateKeyValue("legalRepSigning", "false"));
        kv.add(populateKeyValue("statementOfTruthDateDay", "01"));
        kv.add(populateKeyValue("statementOfTruthDateMonth", "01"));
        kv.add(populateKeyValue("statementOfTruthDateYear", "2022"));
        kv.add(populateKeyValue("soleApplicantOrApplicant1OrLegalRepFullName", "bob builder"));
        kv.add(populateKeyValue("soleOrApplicant1HWFNo", "HWF123"));
        return kv;
    }


    public static List<KeyValue> populateD8OcrDataFields() {
        List<KeyValue> kv = new ArrayList<>(populateCommonOcrDataFields());

        kv.add(populateKeyValue("applicationForDivorce", "true"));
        kv.add(populateKeyValue("prayerMarriageDissolved", "true"));

        return kv;
    }

    public static List<KeyValue> populateD8SOcrDataFields() {
        List<KeyValue> kv = new ArrayList<>(populateCommonOcrDataFields());

        kv.add(populateKeyValue("prayerApplicant1JudiciallySeparated", "true"));

        return kv;
    }


    public static KeyValue populateKeyValue(String key, String value) {
        return KeyValue.builder()
            .key(key)
            .value(value)
            .build();
    }

    public static List<ListValue<ScannedDocument>> scannedDocuments(FormType formType) {
        var scannedDocListValue = ListValue.<ScannedDocument>builder()
            .value(ScannedDocument
                .builder()
                .controlNumber(DOC_CONTROL_NUMBER)
                .deliveryDate(DOC_SCANNED_DATE_META_INFO)
                .scannedDate(DOC_SCANNED_DATE_META_INFO)
                .type(FORM)
                .subtype(formType.getName())
                .fileName(FILE_NAME)
                .url(
                    Document
                        .builder()
                        .binaryUrl(DOCUMENT_BINARY_URL)
                        .url(DOCUMENT_URL)
                        .filename(FILE_NAME)
                        .build()
                )
                .build()
            )
            .id(UUID.randomUUID().toString())
            .build();

        return singletonList(scannedDocListValue);
    }

    public static List<InputScannedDoc> inputScannedDocuments(FormType formType) {
        var inputScannedDoc = InputScannedDoc
            .builder()
            .controlNumber(DOC_CONTROL_NUMBER)
            .scannedDate(DOC_SCANNED_DATE_META_INFO)
            .deliveryDate(DOC_SCANNED_DATE_META_INFO)
            .fileName(FILE_NAME)
            .type("Form")
            .subtype(formType.getName())
            .document(
                InputScannedDocUrl
                    .builder()
                    .url(DOCUMENT_URL)
                    .binaryUrl(DOCUMENT_BINARY_URL)
                    .filename(FILE_NAME)
                    .build()
            )
            .build();
        return singletonList(inputScannedDoc);
    }

    public static CaseData buildCaseDataWithGeneralLetter(GeneralParties recipient) {

        ListValue<DivorceDocument> attachment = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentFileName("some-file")
                .build())
            .build();

        GeneralLetter generalLetter = GeneralLetter.builder()
            .generalLetterParties(recipient)
            .attachments(Lists.newArrayList(attachment))
            .generalLetterDetails("some feedback")
            .build();

        var caseData = validJointApplicant1CaseData();
        caseData.setGeneralLetter(generalLetter);
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.setApplicant2(getApplicantWithAddress());
        return caseData;
    }
}
