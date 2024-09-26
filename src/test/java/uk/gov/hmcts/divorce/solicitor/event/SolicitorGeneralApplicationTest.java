package uk.gov.hmcts.divorce.solicitor.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationSelectFee;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Integer.parseInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType.DEEMED_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorGeneralApplication.SOLICITOR_GENERAL_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getListOfDivorceDocumentListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class SolicitorGeneralApplicationTest {

    private static final String PBA_NUMBER = "PBA0012345";
    private static final String FEE_ACCOUNT_REF = "REF01";

    @Mock
    private GeneralApplicationSelectFee generalApplicationSelectFee;

    @Mock
    private PaymentService paymentService;

    @Mock
    private OrganisationClient organisationClient;

    @Mock
    private HttpServletRequest request;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SolicitorGeneralApplication solicitorGeneralApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorGeneralApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_GENERAL_APPLICATION);
    }

    @Test
    void shouldResetGeneralApplicationWhenAboutToStartCallbackTriggered() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationType(DEEMED_SERVICE)
            .generalApplicationTypeOtherComments("some comments")
            .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setState(Holding);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorGeneralApplication.aboutToStart(details);

        assertThat(response.getData().getGeneralApplication().getGeneralApplicationType()).isNull();
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationTypeOtherComments()).isNull();
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationUrgentCase()).isNull();
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationUrgentCaseReason()).isNull();
        assertThat(response.getData().getGeneralApplication()).isEqualTo(GeneralApplication.builder().build());
    }

    @Test
    void shouldAddGeneralApplicationDocumentToListOfCaseDocumentsAndUpdateState() {
        final DivorceDocument document = DivorceDocument.builder()
                .documentLink(Document.builder().build())
                .build();
        final CaseData caseData = caseData();
        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(1);
        docs.get(0).getValue().setDocumentFileName("Testfile");
        docs.get(0).getValue().setDocumentDateAdded(LOCAL_DATE);
        caseData.getGeneralApplication().setGeneralApplicationDocuments(docs);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setState(Holding);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(GeneralApplicationReceived);
        assertThat(response.getData().getDocuments().getDocumentsUploaded().size()).isEqualTo(1);
        assertThat(response.getData().getDocuments().getDocumentsUploaded().get(0).getValue())
            .isEqualTo(docs.get(0).getValue());
    }

    @Test
    void shouldAddGeneralApplicationDocumentsToListOfCaseDocumentsAndUpdateState() {
        final DivorceDocument document = DivorceDocument.builder()
            .documentLink(Document.builder().build())
            .build();
        final CaseData caseData = caseData();
        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(2);
        docs.get(0).getValue().setDocumentFileName("Testfile");
        docs.get(0).getValue().setDocumentDateAdded(LOCAL_DATE);
        docs.get(1).getValue().setDocumentFileName("Testfile");
        docs.get(1).getValue().setDocumentDateAdded(LOCAL_DATE);
        caseData.getGeneralApplication().setGeneralApplicationDocuments(docs);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setState(Holding);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(GeneralApplicationReceived);
        assertThat(response.getData().getDocuments().getDocumentsUploaded().size()).isEqualTo(2);
        assertThat(response.getData().getDocuments().getDocumentsUploaded().get(0).getValue())
            .isEqualTo(docs.get(1).getValue());
        assertThat(response.getData().getDocuments().getDocumentsUploaded().get(1).getValue())
            .isEqualTo(docs.get(0).getValue());
    }

    @Test
    void shouldSetGeneralDocumentTypeForUploadedDocuments() {
        final CaseData caseData = caseData();
        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(1);
        docs.get(0).getValue().setDocumentFileName("Testfile");
        docs.get(0).getValue().setDocumentDateAdded(LOCAL_DATE);
        caseData.getGeneralApplication().setGeneralApplicationDocuments(docs);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setState(Holding);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getData().getGeneralApplication().getGeneralApplicationDocuments().get(0)
            .getValue().getDocumentType()).isEqualTo(DocumentType.GENERAL_APPLICATION);
    }

    @Test
    void shouldAddPaymentToApplicationPaymentsIfPaymentSuccessful() {
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        final Payment payment = Payment
            .builder()
            .amount(parseInt(orderSummary.getPaymentTotal()))
            .channel("online")
            .created(LocalDateTime.now())
            .feeCode("FEE0001")
            .reference(orderSummary.getPaymentReference())
            .status(PaymentStatus.SUCCESS)
            .transactionId("Transaction1")
            .build();
        final ListValue<Payment> paymentListValue = ListValue
            .<Payment>builder()
            .id(UUID.randomUUID().toString())
            .value(payment)
            .build();
        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);
        final CaseData caseData = validApplicant1CaseData();
        caseData.getApplication().setApplicationPayments(payments);

        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(1);
        docs.get(0).getValue().setDocumentFileName("Testfile");
        docs.get(0).getValue().setDocumentDateAdded(LOCAL_DATE);

        final OrderSummary generalApplicationOrderSummary = OrderSummary.builder()
            .paymentTotal("500")
            .fees(List.of(ListValue
                .<Fee>builder()
                .id("1")
                .value(Fee.builder()
                    .code("fee code")
                    .build())
                .build())
            )
            .build();

        caseData.setGeneralApplication(
            GeneralApplication.builder()
                .generalApplicationFee(
                    FeeDetails.builder()
                        .orderSummary(generalApplicationOrderSummary)
                        .accountReferenceNumber(FEE_ACCOUNT_REF)
                        .pbaNumbers(
                            DynamicList.builder()
                                .value(
                                    DynamicListElement.builder()
                                        .label(PBA_NUMBER)
                                        .build())
                                .build()
                        )
                        .paymentMethod(FEE_PAY_BY_ACCOUNT)
                        .build()
                )
                .generalApplicationDocuments(docs)
                .build()
        );

        final Solicitor applicant1Solicitor = Solicitor.builder()
            .organisationPolicy(
                OrganisationPolicy.<UserRole>builder()
                    .organisation(Organisation.builder()
                        .organisationId("App1OrgPolicy")
                        .build())
                    .build()
            )
            .build();
        final Solicitor applicant2Solicitor = Solicitor.builder()
            .organisationPolicy(
                OrganisationPolicy.<UserRole>builder()
                    .organisation(Organisation.builder()
                        .organisationId("App2OrgPolicy")
                        .build())
                    .build()
            )
            .build();
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(applicant1Solicitor);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(applicant2Solicitor);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        final var organisationsResponse = mock(OrganisationsResponse.class);

        when(request.getHeader(AUTHORIZATION)).thenReturn(AUTH_HEADER_VALUE);
        when(authTokenGenerator.generate()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(organisationClient.getUserOrganisation(AUTH_HEADER_VALUE, TEST_AUTHORIZATION_TOKEN))
            .thenReturn(organisationsResponse);
        when(organisationsResponse.getOrganisationIdentifier()).thenReturn("App1OrgPolicy");

        final var pbaResponse = new PbaResponse(CREATED, null, "1234");
        when(paymentService.processPbaPayment(
            caseData,
            TEST_CASE_ID,
            applicant1Solicitor,
            PBA_NUMBER,
            generalApplicationOrderSummary,
            FEE_ACCOUNT_REF)
        ).thenReturn(pbaResponse);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getApplication().getApplicationPayments()).hasSize(2);
    }

    @Test
    void shouldReturnErrorsIfCaseIsCurrentlyLinkedToActiveBulkCase() {
        final CaseData caseData = caseData();
        caseData.setBulkListCaseReferenceLink(CaseLink
            .builder()
            .caseReference("1234")
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setState(AwaitingPronouncement);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors())
            .contains("General Application cannot be submitted as this case is currently linked to an active bulk action case");
    }

    @Test
    void shouldReturnErrorsIfSolicitorOrganisationPolicyDoesNotMatchOneOnCase() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(
            GeneralApplication.builder()
                .generalApplicationFee(
                    FeeDetails.builder()
                        .paymentMethod(FEE_PAY_BY_ACCOUNT)
                        .build())
                .build()
        );
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .organisationPolicy(
                    OrganisationPolicy.<UserRole>builder()
                        .organisation(Organisation.builder()
                            .organisationId("App1OrgPolicy")
                            .build())
                        .build()
                )
                .build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .organisationPolicy(
                    OrganisationPolicy.<UserRole>builder()
                        .organisation(Organisation.builder()
                            .organisationId("App2OrgPolicy")
                            .build())
                        .build()
                )
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        final var organisationsResponse = mock(OrganisationsResponse.class);

        when(request.getHeader(AUTHORIZATION)).thenReturn(AUTH_HEADER_VALUE);
        when(authTokenGenerator.generate()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(organisationClient.getUserOrganisation(AUTH_HEADER_VALUE, TEST_AUTHORIZATION_TOKEN))
            .thenReturn(organisationsResponse);
        when(organisationsResponse.getOrganisationIdentifier()).thenReturn("OrgPolicy");

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors())
            .contains(
                "General Application payment could not be completed as the invokers organisation policy did not match any on the case"
            );
    }

    @Test
    void shouldReturnErrorsIfPaymentIsUnsuccessful() {
        final CaseData caseData = caseData();
        final Solicitor applicant2Solicitor = Solicitor.builder()
            .organisationPolicy(
                OrganisationPolicy.<UserRole>builder()
                    .organisation(Organisation.builder()
                        .organisationId("App2OrgPolicy")
                        .build())
                    .build()
            )
            .build();
        final OrderSummary generalApplicationOrderSummary = OrderSummary.builder()
            .paymentTotal("500")
            .fees(List.of(ListValue
                .<Fee>builder()
                .id("1")
                .value(Fee.builder()
                    .code("fee code")
                    .build())
                .build())
            )
            .build();
        caseData.setGeneralApplication(
            GeneralApplication.builder()
                .generalApplicationFee(
                    FeeDetails.builder()
                        .orderSummary(generalApplicationOrderSummary)
                        .accountReferenceNumber(FEE_ACCOUNT_REF)
                        .pbaNumbers(
                            DynamicList.builder()
                                .value(
                                    DynamicListElement.builder()
                                        .label(PBA_NUMBER)
                                        .build())
                                .build()
                        )
                        .paymentMethod(FEE_PAY_BY_ACCOUNT)
                        .build())
                .build()
        );
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .organisationPolicy(
                    OrganisationPolicy.<UserRole>builder()
                        .organisation(Organisation.builder()
                            .organisationId("App1OrgPolicy")
                            .build())
                        .build()
                )
                .build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(applicant2Solicitor);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        final var organisationsResponse = mock(OrganisationsResponse.class);

        when(request.getHeader(AUTHORIZATION)).thenReturn(AUTH_HEADER_VALUE);
        when(authTokenGenerator.generate()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(organisationClient.getUserOrganisation(AUTH_HEADER_VALUE, TEST_AUTHORIZATION_TOKEN))
            .thenReturn(organisationsResponse);
        when(organisationsResponse.getOrganisationIdentifier()).thenReturn("App2OrgPolicy");

        final var pbaResponse = new PbaResponse(FORBIDDEN, "Account balance insufficient", null);
        when(paymentService.processPbaPayment(
            caseData,
            TEST_CASE_ID,
            applicant2Solicitor,
            PBA_NUMBER,
            generalApplicationOrderSummary,
            FEE_ACCOUNT_REF
        )).thenReturn(pbaResponse);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors())
            .contains("Account balance insufficient");
    }

    @Test
    void shouldReturnErrorIfUrgentFlagIsSetButNoReasonProvided() {
        final DivorceDocument document = DivorceDocument.builder()
            .documentLink(Document.builder().build())
            .build();
        final CaseData caseData = caseData();
        caseData.getGeneralApplication().setGeneralApplicationUrgentCase(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setState(Holding);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorGeneralApplication.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors())
            .contains("General Application marked as urgent need an accompanying reason why it is urgent");
    }
}
