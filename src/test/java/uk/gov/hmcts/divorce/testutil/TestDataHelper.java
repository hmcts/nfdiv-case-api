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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkscan.endpoint.data.FormType;
import uk.gov.hmcts.divorce.bulkscan.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.InputScannedDoc;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.InputScannedDocUrl;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.OcrDataField;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
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
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;
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
import java.util.stream.Collectors;

import static feign.Request.HttpMethod.GET;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.FORM;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationRemindApplicant2Notification.APPLICANT_2_SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.citizen.notification.ApplicationRemindApplicant2Notification.APPLICANT_2_SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.DissolveDivorce.DISSOLVE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.builder;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.NA;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICANT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CIVIL_PARTNER_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.HUSBAND_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.RESPONDENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.WIFE_JOINT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlementHelper.IS_JOINT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_SIGN_IN_DISSOLUTION_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DISSOLUTION_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_FIRM_NAME;
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

    public static Applicant getApplicant2WithAddress() {
        return Applicant.builder()
            .firstName(TEST_APP2_FIRST_NAME)
            .middleName(TEST_APP2_MIDDLE_NAME)
            .lastName(TEST_APP2_LAST_NAME)
            .email(TEST_APPLICANT_2_USER_EMAIL)
            .gender(FEMALE)
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
            .firstName(TEST_APP2_FIRST_NAME)
            .middleName(TEST_APP2_MIDDLE_NAME)
            .lastName(TEST_APP2_LAST_NAME)
            .solicitor(
                Solicitor.builder().build()
            )
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

    public static Solicitor getOfflineSolicitor() {
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1("sol line1")
            .addressLine2("sol line2")
            .postTown("sol city")
            .postCode("sol postcode")
            .build();

        return Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .firmName(TEST_SOLICITOR_FIRM_NAME)
            .address(addressGlobalUK.toString())
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

    public static CaseData caseDataWithMarriageDate() {
        MarriageDetails marriageDetails = MarriageDetails.builder().date(LOCAL_DATE).build();
        return CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(getApplicant())
            .divorceOrDissolution(DIVORCE)
            .supplementaryCaseType(NA)
            .application(Application.builder().marriageDetails(marriageDetails).build())
            .caseInvite(new CaseInvite(null, null, null))
            .build();
    }

    public static CaseData caseData() {
        return CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(getApplicant())
            .divorceOrDissolution(DIVORCE)
            .supplementaryCaseType(NA)
            .caseInvite(new CaseInvite(null, null, null))
            .build();
    }

    public static CaseData invalidCaseData() {
        return CaseData.builder()
            .applicant1(getInvalidApplicant())
            .divorceOrDissolution(DIVORCE)
            .supplementaryCaseType(NA)
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
        caseData.getApplicant1().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(ApplicantPrayer.DissolveDivorce.DISSOLVE_DIVORCE));

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
            .supplementaryCaseType(NA)
            .build();
    }

    public static CaseData validApplicant1CaseData() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplicant2(getApplicant2(MALE));
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplicant1().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE));
        return caseData;
    }

    public static CaseData validApplicant2CaseData() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplicant1().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE));
        caseData.setApplicant2(getApplicantWithAddress());
        caseData.getApplication().setApplicant2HelpWithFees(HelpWithFees.builder()
            .needHelp(NO)
            .build());
        caseData.getApplicant2().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE));
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
        applicant1.setApplicantPrayer(ApplicantPrayer.builder().prayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE)).build());

        var application = Application.builder()
            .solSignStatementOfTruth(YES)
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
        application.setServiceMethod(COURT_SERVICE);
        application.setDateSubmitted(LocalDateTime.now());
        application.setDocumentUploadComplete(YES);
        application.setMarriageDetails(marriageDetails);
        application.setApplicant1StatementOfTruth(YES);
        application.setJurisdiction(getJurisdiction());
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setFinancialOrder(NO);
        caseData.getApplicant1().setLegalProceedings(NO);
        caseData.getApplicant1().getApplicantPrayer().setPrayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE));
        caseData.setCaseInvite(new CaseInvite(TEST_APPLICANT_2_USER_EMAIL, null, null));

        return caseData;
    }

    public static CaseData validCaseDataForReIssueApplication() {
        CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.now());

        return caseData;
    }

    public static CaseData validCaseDataForAosSubmitted() {
        CaseData caseData = validCaseDataForIssueApplication();

        caseData.getApplicant2().setLegalProceedings(NO);
        caseData.getAcknowledgementOfService().setConfirmReadPetition(YES);
        caseData.getAcknowledgementOfService().setJurisdictionAgree(YES);
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
                    .caseTypeId(BulkActionCaseTypeConfig.getCaseType())
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
                    .caseTypeId(getCaseType())
                    .build()
            )
            .build();
    }

    public static CallbackRequest callbackRequest(final CaseData caseData, String eventId, String state) {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        CaseDetails caseDetailsBefore = caseDetailsBefore(caseData);
        caseDetailsBefore.setState(state);
        return CallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetailsBefore(caseDetailsBefore)
            .caseDetails(
                CaseDetails
                    .builder()
                    .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
                    .state(state)
                    .id(TEST_CASE_ID)
                    .caseTypeId(getCaseType())
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
                    .caseTypeId(getCaseType())
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

    public static ListValue<ScannedDocument> scannedDocumentWithType(final ScannedDocumentType documentType,
                                                                     final String documentId) {
        final String documentUrl = "http://localhost:8080/" + documentId;

        final Document ccdDocument = new Document(
            documentUrl,
            "test-draft-divorce-application.pdf",
            documentUrl + "/binary"
        );

        final ScannedDocument divorceDocument = ScannedDocument
            .builder()
            .url(ccdDocument)
            .fileName("test-draft-divorce-application-12345.pdf")
            .type(documentType)
            .build();

        return ListValue
            .<ScannedDocument>builder()
            .id(APPLICATION.getLabel())
            .value(divorceDocument)
            .build();
    }

    public static ListValue<ConfidentialDivorceDocument> confidentialDocumentWithType(final ConfidentialDocumentsReceived documentType) {
        return confidentialDocumentWithType(documentType, UUID.randomUUID().toString());
    }

    public static ListValue<ConfidentialDivorceDocument> confidentialDocumentWithType(final ConfidentialDocumentsReceived documentType,
                                                                                      final String documentId) {
        String documentUrl = "http://localhost:8080/" + documentId;

        Document ccdDocument = new Document(
            documentUrl,
            "test-draft-divorce-application.pdf",
            documentUrl + "/binary"
        );

        ConfidentialDivorceDocument confidentialDivorceDocument = ConfidentialDivorceDocument.builder()
            .documentLink(ccdDocument)
            .documentFileName("test-draft-divorce-application-12345.pdf")
            .confidentialDocumentsReceived(documentType)
            .build();

        return ListValue
            .<ConfidentialDivorceDocument>builder()
            .id(documentType.getLabel())
            .value(confidentialDivorceDocument)
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
        templateVars.put(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID);
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
        templateVars.put(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID);
        templateVars.put(SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        templateVars.put(APPLICANT_NAME, join(" ", TEST_FIRST_NAME, TEST_LAST_NAME));
        templateVars.put(RESPONDENT_NAME, join(" ", APPLICANT_2_FIRST_NAME, APPLICANT_2_LAST_NAME));
        return templateVars;
    }

    public static Map<String, String> solicitorTemplateVarsPreIssue(CaseData data, Applicant applicant) {
        Map<String, String> templateVars = getBasicTemplateVars();
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(SOLICITOR_REFERENCE,
            isNotEmpty(applicant.getSolicitor().getReference())
                ? applicant.getSolicitor().getReference()
                : NOT_PROVIDED);
        templateVars.put(APPLICANT_1_FULL_NAME, data.getApplicant1().getFullName());
        templateVars.put(APPLICANT_2_FULL_NAME, data.getApplicant2().getFullName());
        templateVars.put(SIGN_IN_URL, getConfigTemplateVars().get(SIGN_IN_DIVORCE_URL));
        return templateVars;
    }

    public static Map<String, String> solicitorTemplateVars(CaseData data, Applicant applicant) {
        Map<String, String> templateVars = solicitorTemplateVarsPreIssue(data, applicant);
        templateVars.put(ISSUE_DATE, data.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));
        return templateVars;
    }

    public static Map<String, Object> getBasicDocmosisTemplateContent(LanguagePreference languagePreference) {
        Map<String, Object> basicDocmosisTemplateContent = new HashMap<>();

        if (ENGLISH.equals(languagePreference)) {
            basicDocmosisTemplateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
            basicDocmosisTemplateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
            basicDocmosisTemplateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
            basicDocmosisTemplateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        } else {
            basicDocmosisTemplateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY);
            basicDocmosisTemplateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY);
            basicDocmosisTemplateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
            basicDocmosisTemplateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY);
        }
        return basicDocmosisTemplateContent;
    }

    public static CtscContactDetails getCtscContactDetails() {
        return CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce and Dissolution")
            .serviceCentre("Courts and Tribunals Service Centre")
            .poBox("PO Box 13226")
            .town("Harlow")
            .postcode("CM20 9UG")
            .emailAddress("contactdivorce@justice.gov.uk")
            .phoneNumber("0300 303 0642")
            .build();
    }

    public static Map<String, Object> getBasicDocmosisTemplateContentWithCtscContactDetails(LanguagePreference languagePreference) {
        Map<String, Object> basicDocmosisTemplateContent = getBasicDocmosisTemplateContent(languagePreference);

        basicDocmosisTemplateContent.put(CTSC_CONTACT_DETAILS, getCtscContactDetails());

        return basicDocmosisTemplateContent;
    }

    public static Map<String, Object> getSolicitorDocTemplateContent(CaseData data, Applicant applicant) {
        Map<String, Object> templateVars = getBasicDocmosisTemplateContent(applicant.getLanguagePreference());
        templateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateVars.put(DocmosisTemplateConstants.SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(SOLICITOR_REFERENCE, applicant.getSolicitor().getReference());
        templateVars.put(SOLICITOR_ADDRESS, applicant.getSolicitor().getAddress());
        templateVars.put(IS_DIVORCE, data.isDivorce());
        templateVars.put(IS_JOINT, !data.getApplicationType().isSole());
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
            .generalOrderJudgeOrLegalAdvisorVenue("Petty France, London")
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
            .caseTypeId(getCaseType())
            .build();
    }

    private static CaseDetails caseDetailsBefore(BulkActionCaseData caseData) {
        return CaseDetails
            .builder()
            .data(OBJECT_MAPPER.convertValue(caseData, TYPE_REFERENCE))
            .id(TEST_CASE_ID)
            .caseTypeId(BulkActionCaseTypeConfig.getCaseType())
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
                    new OcrDataField("applicant1Name", "bob")
            ))
            .build();
    }

    public static List<OcrDataField> populateCommonOcrDataFields() {
        List<OcrDataField> ocrDataFields = new ArrayList<>();
        ocrDataFields.add(new OcrDataField("aSoleApplication", "true"));
        ocrDataFields.add(new OcrDataField("marriageOrCivilPartnershipCertificate", "true"));
        ocrDataFields.add(new OcrDataField("translation", "false"));
        ocrDataFields.add(new OcrDataField("soleApplicantOrApplicant1FirstName", "bob"));
        ocrDataFields.add(new OcrDataField("soleApplicantOrApplicant1LastName", "builder"));
        ocrDataFields.add(new OcrDataField("respondentOrApplicant2FirstName", "the"));
        ocrDataFields.add(new OcrDataField("respondentOrApplicant2LastName", "respondent"));
        ocrDataFields.add(new OcrDataField("respondentOrApplicant2MarriedName", "No"));
        ocrDataFields.add(new OcrDataField("serveOutOfUK", "Yes"));
        ocrDataFields.add(new OcrDataField("respondentServePostOnly", "true"));
        ocrDataFields.add(new OcrDataField("respondentDifferentServiceAddress", "No"));
        ocrDataFields.add(new OcrDataField("marriageOutsideOfUK", "No"));
        ocrDataFields.add(new OcrDataField("dateOfMarriageOrCivilPartnershipDay", "01"));
        ocrDataFields.add(new OcrDataField("dateOfMarriageOrCivilPartnershipMonth", "01"));
        ocrDataFields.add(new OcrDataField("dateOfMarriageOrCivilPartnershipYear", "1990"));
        ocrDataFields.add(new OcrDataField("soleOrApplicant1FullNameAsOnCert", "bob builder"));
        ocrDataFields.add(new OcrDataField("respondentOrApplicant2FullNameAsOnCert", "the respondent"));
        ocrDataFields.add(new OcrDataField("detailsOnCertCorrect", "Yes"));
        ocrDataFields.add(new OcrDataField("jurisdictionReasonsBothPartiesHabitual", "true"));
        ocrDataFields.add(new OcrDataField("existingOrPreviousCourtCases", "No"));
        ocrDataFields.add(new OcrDataField("soleOrApplicant1FinancialOrder", "No"));
        ocrDataFields.add(new OcrDataField("soleOrApplicant1ConfirmationOfBreakdown", "true"));
        ocrDataFields.add(new OcrDataField("soleApplicantOrApplicant1StatementOfTruth", "true"));
        ocrDataFields.add(new OcrDataField("soleApplicantOrApplicant1LegalRepStatementOfTruth", "true"));
        ocrDataFields.add(new OcrDataField("soleApplicantOrApplicant1OrLegalRepSignature", "signed"));
        ocrDataFields.add(new OcrDataField("soleApplicantOrApplicant1Signing", "true"));
        ocrDataFields.add(new OcrDataField("legalRepSigning", "false"));
        ocrDataFields.add(new OcrDataField("statementOfTruthDateDay", "01"));
        ocrDataFields.add(new OcrDataField("statementOfTruthDateMonth", "01"));
        ocrDataFields.add(new OcrDataField("statementOfTruthDateYear", "2022"));
        ocrDataFields.add(new OcrDataField("soleApplicantOrApplicant1OrLegalRepFullName", "bob builder"));
        ocrDataFields.add(new OcrDataField("soleOrApplicant1HWFNo", "HWF123"));
        return ocrDataFields;
    }


    public static List<OcrDataField> populateD8OcrDataFields() {
        List<OcrDataField> ocrDataFields = new ArrayList<>(populateCommonOcrDataFields());

        ocrDataFields.add(new OcrDataField("applicationForDivorce", "true"));
        ocrDataFields.add(new OcrDataField("prayerMarriageDissolved", "true"));

        return ocrDataFields;
    }

    public static List<OcrDataField> populateD8SOcrDataFields() {
        List<OcrDataField> ocrDataFields = new ArrayList<>(populateCommonOcrDataFields());

        ocrDataFields.add(new OcrDataField("prayerApplicant1JudiciallySeparated", "true"));

        return ocrDataFields;
    }


    public static List<ListValue<ScannedDocument>> scannedDocuments(FormType formType) {
        return scannedDocuments(formType.getName());
    }

    public static List<ListValue<ScannedDocument>> scannedDocuments(List<String> scannedDocumentSubtypes) {
        return scannedDocumentSubtypes
            .stream()
            .map(TestDataHelper::scannedDocuments)
            .flatMap(java.util.Collection::stream)
            .collect(Collectors.toList());
    }

    private static List<ListValue<ScannedDocument>> scannedDocuments(String subtype) {
        var scannedDocListValue = ListValue.<ScannedDocument>builder()
            .value(ScannedDocument
                .builder()
                .controlNumber(DOC_CONTROL_NUMBER)
                .deliveryDate(DOC_SCANNED_DATE_META_INFO)
                .scannedDate(DOC_SCANNED_DATE_META_INFO)
                .type(FORM)
                .subtype(subtype)
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

    public static List<ListValue<DivorceDocument>> getListOfDivorceDocumentListValue(int sizeOfList) {
        List<ListValue<DivorceDocument>> docList = new ArrayList<>();
        while (sizeOfList > 0) {
            ListValue<DivorceDocument> documentListValue = new ListValue<>(
                UUID.randomUUID().toString(),
                DivorceDocument
                    .builder()
                    .documentLink(Document.builder()
                        .url("http://localhost:4200/assets/d11")
                        .filename("dummy.file")
                        .binaryUrl("dummy.file/binary")
                        .build())
                    .build()
            );
            docList.add(documentListValue);
            sizeOfList--;
        }
        return docList;
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
                .documentFileName("some-file.pdf")
                .documentLink(Document.builder()
                    .filename("some-file.pdf")
                    .url("http://localhost:8080/1234")
                    .binaryUrl("http://localhost:8080/1234/binary")
                    .build())
                .build())
            .build();

        GeneralLetter generalLetter = GeneralLetter.builder()
            .generalLetterParties(recipient)
            .generalLetterAttachments(Lists.newArrayList(attachment))
            .generalLetterDetails("some feedback")
            .build();

        var caseData = validJointApplicant1CaseData();
        caseData.setGeneralLetter(generalLetter);
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.setApplicant2(getApplicantWithAddress());
        return caseData;
    }


    public static CaseData buildCaseDataForGrantFinalOrder(ApplicationType applicationType, DivorceOrDissolution divorceOrDissolution) {
        var caseData = validCaseDataForAwaitingFinalOrder();
        caseData.setApplicationType(applicationType);
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().getMarriageDetails().setCountryOfMarriage("United Kingdom");
        caseData.getConditionalOrder().setGrantedDate(LocalDate.of(2022, 3, 10));
        caseData.setDivorceOrDissolution(divorceOrDissolution);
        return caseData;
    }

    public static CaseData getConfirmServiceCaseData() {
        LocalDate issueDate = LocalDate.of(2022, 8, 10);
        LocalDate serviceDate = LocalDate.of(2022, 8, 12);
        final var caseData = caseData();
        caseData.getApplication().setIssueDate(issueDate);
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplication().setSolicitorService(SolicitorService.builder()
            .serviceDetails("service details")
            .dateOfService(serviceDate)
            .addressServed("address served")
            .documentsServed("docs served")
            .build());
        caseData.setApplicationType(SOLE_APPLICATION);

        final ListValue<DivorceDocument> confirmServiceAttachments = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentLink(new Document("url", "filename.pdf", "url/binary"))
                .build())
            .build();

        caseData.getDocuments().setDocumentsUploadedOnConfirmService(Lists.newArrayList(confirmServiceAttachments));

        return caseData;
    }

    public static Map<String, String> getFinalOrderSolicitorsVars(CaseData caseData, Applicant applicant) {
        Map<String, String> templateVars = solicitorTemplateVars(caseData, applicant);
        templateVars.put(IS_CONDITIONAL_ORDER, CommonContent.NO);
        templateVars.put(IS_FINAL_ORDER, CommonContent.YES);
        return templateVars;
    }

    public static CaseData buildCaseDataCOPronounced(final YesOrNo isOffline, final ContactDetailsType app1ContactType,
                                                     final ContactDetailsType app2ContactType) {
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1("line1")
            .addressLine2("line2")
            .postTown("city")
            .postCode("postcode")
            .build();

        Applicant applicant1 = Applicant.builder()
            .address(addressGlobalUK)
            .firstName("Bob")
            .lastName("Smith")
            .offline(isOffline)
            .contactDetailsType(app1ContactType)
            .build();

        Applicant applicant2 = Applicant.builder()
            .firstName("Lily")
            .lastName("Jones")
            .address(addressGlobalUK)
            .offline(isOffline)
            .contactDetailsType(app2ContactType)
            .build();

        ListValue<DivorceDocument> coGrantedDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED)
                .build())
            .build();

        ListValue<ConfidentialDivorceDocument> coConfidentialCoverLetterApp1 = ListValue.<ConfidentialDivorceDocument>builder()
            .value(ConfidentialDivorceDocument.builder()
                .confidentialDocumentsReceived(
                    ConfidentialDocumentsReceived.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1)
                .build())
            .build();

        ListValue<ConfidentialDivorceDocument> coConfidentialCoverLetterApp2 = ListValue.<ConfidentialDivorceDocument>builder()
            .value(ConfidentialDivorceDocument.builder()
                .confidentialDocumentsReceived(
                    ConfidentialDocumentsReceived.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2)
                .build())
            .build();

        ListValue<DivorceDocument> coCoverLetterApp1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1)
                .build())
            .build();

        ListValue<DivorceDocument> coCoverLetterApp2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2)
                .build())
            .build();

        if (applicant1.isConfidentialContactDetails() && applicant2.isConfidentialContactDetails()) {
            return CaseData.builder()
                .divorceOrDissolution(DIVORCE)
                .applicant1(applicant1)
                .applicant2(applicant2)
                .conditionalOrder(ConditionalOrder.builder()
                    .court(ConditionalOrderCourt.BIRMINGHAM)
                    .dateAndTimeOfHearing(LOCAL_DATE_TIME)
                    .grantedDate(LOCAL_DATE)
                    .build())
                .documents(builder()
                    .documentsGenerated(Lists.newArrayList(coGrantedDoc))
                    .confidentialDocumentsGenerated(
                        Lists.newArrayList(coConfidentialCoverLetterApp1, coConfidentialCoverLetterApp2))
                    .build())
                .build();
        }
        return CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .conditionalOrder(ConditionalOrder.builder()
                .court(ConditionalOrderCourt.BIRMINGHAM)
                .dateAndTimeOfHearing(LOCAL_DATE_TIME)
                .grantedDate(LOCAL_DATE)
                .build())
            .documents(builder()
                .documentsGenerated(Lists.newArrayList(coGrantedDoc, coCoverLetterApp1, coCoverLetterApp2))
                .build())
            .build();
    }
}
