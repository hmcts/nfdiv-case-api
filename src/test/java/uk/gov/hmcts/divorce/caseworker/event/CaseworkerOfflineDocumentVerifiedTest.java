package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant1AppliedForConditionalOrderNotification;
import uk.gov.hmcts.divorce.common.service.GeneralReferralService;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.common.service.SubmitAosService;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.FORM;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerOfflineDocumentVerified.CASEWORKER_OFFLINE_DOCUMENT_VERIFIED;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleCo.SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleFinalOrderOffline.SWITCH_TO_SOLE_FO_OFFLINE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.AOS_D10;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.CO_D84;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.FO_D36;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D10;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D36;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D84;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D84NVA;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineApplicationType.JOINT;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineApplicationType.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAmendedApplication;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InBulkActionCase;
import static uk.gov.hmcts.divorce.divorcecase.model.State.JSAwaitingLA;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RespondentFinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CaseworkerOfflineDocumentVerifiedTest {

    @Mock
    private SubmitAosService submitAosService;

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private Applicant1AppliedForConditionalOrderNotification app1AppliedForConditionalOrderNotification;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private Clock clock;

    @Mock
    private GeneralReferralService generalReferralService;


    @InjectMocks
    private CaseworkerOfflineDocumentVerified caseworkerOfflineDocumentVerified;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerOfflineDocumentVerified.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_OFFLINE_DOCUMENT_VERIFIED);
    }

    @Test
    void shouldSetStateToHoldingAndReclassifyAosScannedDocumentToRespondentAnswersIfD10DocumentSelectedAndFoundInScannedDocNames() {
        setMockClock(clock);
        final ListValue<ScannedDocument> doc1 = ListValue.<ScannedDocument>builder()
            .value(
                ScannedDocument
                    .builder()
                    .url(
                        Document
                            .builder()
                            .filename("doc1.pdf")
                            .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                            .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                            .build()
                    )
                    .fileName("doc1.pdf")
                    .type(ScannedDocumentType.OTHER)
                    .subtype("aos")
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();

        CaseData caseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .typeOfDocumentAttached(AOS_D10)
                    .scannedDocuments(singletonList(doc1))
                    .scannedDocumentNames(
                        DynamicList
                            .builder()
                            .value(
                                DynamicListElement
                                    .builder()
                                    .label("doc1.pdf")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .howToRespondApplication(DISPUTE_DIVORCE)
                .build())
            .build();

        details.setData(caseData);

        final ListValue<DivorceDocument> doc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(RESPONDENT_ANSWERS)
                .documentFileName("doc1.pdf")
                .documentComment("Reclassified scanned document")
                .documentDateAdded(getExpectedLocalDate())
                .documentLink(Document
                    .builder()
                    .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                    .filename("doc1.pdf")
                    .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                    .build()
                )
                .build())
            .build();

        List<ListValue<DivorceDocument>> documentsUploaded = new ArrayList<>();
        documentsUploaded.add(doc);

        final CaseDetails<CaseData, State> updatedDetails = new CaseDetails<>();
        updatedDetails.setData(CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .documentsUploaded(documentsUploaded)
                    .build()
            )
            .applicant2(Applicant.builder()
                .build())
            .build());
        updatedDetails.setState(Holding);

        when(submitAosService.submitOfflineAos(details)).thenReturn(updatedDetails);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState().name()).isEqualTo(Holding.name());
        assertThat(response.getData().getApplicant2().getOffline()).isEqualTo(YES);

        verify(submitAosService).submitOfflineAos(details);
        verify(submitAosService).submitAosNotifications(details);
        verifyNoMoreInteractions(submitAosService);

        Document ccdDocument = new Document(
            "http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d",
            "doc1.pdf",
            "http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary"
        );

        var divorceDocument = DivorceDocument
            .builder()
            .documentLink(ccdDocument)
            .documentFileName("doc1.pdf")
            .documentComment("Reclassified scanned document")
            .documentDateAdded(getExpectedLocalDate())
            .documentType(RESPONDENT_ANSWERS)
            .build();

        assertThat(response.getData().getDocuments().getDocumentsUploaded())
            .extracting("value")
            .containsExactly(divorceDocument);
        assertThat(response.getData().getAcknowledgementOfService().getStatementOfTruth()).isEqualTo(YES);
    }

    @Test
    void shouldSetStateToHoldingAndSkipDocumentReclassificationIfD10DocumentSelectedAndScannedSubtypeReceivedIsD10() {

        final ListValue<ScannedDocument> doc1 = ListValue.<ScannedDocument>builder()
            .value(
                ScannedDocument
                    .builder()
                    .url(
                        Document
                            .builder()
                            .filename("doc1.pdf")
                            .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                            .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                            .build()
                    )
                    .fileName("doc1.pdf")
                    .type(ScannedDocumentType.OTHER)
                    .subtype("d10")
                    .build()
            )
            .build();

        final ListValue<DivorceDocument> doc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(RESPONDENT_ANSWERS)
                .documentFileName("doc1.pdf")
                .documentComment("Reclassified scanned document")
                .documentDateAdded(getExpectedLocalDate())
                .documentLink(Document
                    .builder()
                    .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                    .filename("doc1.pdf")
                    .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                    .build()
                )
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();

        CaseData caseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .documentsUploaded(singletonList(doc))
                    .scannedSubtypeReceived(D10)
                    .scannedDocuments(singletonList(doc1))
                    .typeOfDocumentAttached(AOS_D10)
                    .build()
            )
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .howToRespondApplication(DISPUTE_DIVORCE)
                .build())
            .build();

        details.setData(caseData);

        final CaseDetails<CaseData, State> updatedDetails = new CaseDetails<>();
        updatedDetails.setData(CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .documentsUploaded(singletonList(doc))
                    .scannedSubtypeReceived(D10)
                    .scannedDocuments(singletonList(doc1))
                    .build()
            )
            .applicant2(Applicant.builder()
                .build())
            .build());
        updatedDetails.setState(Holding);

        when(submitAosService.submitOfflineAos(details)).thenReturn(updatedDetails);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState().name()).isEqualTo(Holding.name());
        assertThat(response.getData().getApplicant2().getOffline()).isEqualTo(YES);
        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNull();

        verify(submitAosService).submitOfflineAos(details);
        verify(submitAosService).submitAosNotifications(details);
        verifyNoMoreInteractions(submitAosService);
    }

    @Test
    void shouldSetStateToHoldingAndSkipReclassifyIfSelectedD10DocumentIsNotFoundInScannedDocNames() {
        final ListValue<ScannedDocument> doc1 = scannedDocument("doc1.pdf");
        final CaseDetails<CaseData, State> details = new CaseDetails<>();

        CaseData caseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .typeOfDocumentAttached(AOS_D10)
                    .scannedDocuments(singletonList(doc1))
                    .scannedDocumentNames(
                        DynamicList
                            .builder()
                            .value(
                                DynamicListElement
                                    .builder()
                                    .label("doc2.pdf")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .howToRespondApplication(DISPUTE_DIVORCE)
                .build())
            .build();

        details.setData(caseData);


        final CaseDetails<CaseData, State> updatedDetails = new CaseDetails<>();
        updatedDetails.setData(CaseData.builder()
            .applicant2(Applicant.builder()
                .build())
            .build());
        updatedDetails.setState(Holding);

        when(submitAosService.submitOfflineAos(details)).thenReturn(updatedDetails);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState().name()).isEqualTo(Holding.name());
        assertThat(response.getData().getApplicant2().getOffline()).isEqualTo(YES);
        assertThat(response.getData().getDocuments().getDocumentsUploaded()).isNull();

        verify(submitAosService).submitOfflineAos(details);
        verify(submitAosService).submitAosNotifications(details);
        verifyNoMoreInteractions(submitAosService);
    }

    @Test
    void shouldSetStateToUserValueProvidedIfTypeOfDocumentSelectedIsOther() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().typeOfDocumentAttached(OTHER).scannedSubtypeReceived(D84NVA).build())
            .application(Application.builder()
                .stateToTransitionApplicationTo(AwaitingAmendedApplication)
                .build())
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .build())
            .build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState().name()).isEqualTo(AwaitingAmendedApplication.name());
        assertThat(response.getData().getAcknowledgementOfService().getStatementOfTruth()).isNull();
        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNull();
    }

    // TODO: NFDIV-3869 - InBulkActionCase is on hold until we figure out how to properly allow caseworkers to edit cases in bulk lists.
    //  If chosen, we override it to be AwaitPronouncement. This test should be removed once new logic is added to allow use of state.
    @Test
    void shouldOverrideStateToAwaitingPronouncementIfStateIsInBulkActionCase() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().typeOfDocumentAttached(OTHER).scannedSubtypeReceived(D84NVA).build())
            .application(Application.builder()
                .stateToTransitionApplicationTo(InBulkActionCase)
                .build())
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .build())
            .build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState().name()).isEqualTo(AwaitingPronouncement.name());
    }

    @Test
    void shouldSetStateToUserValueProvidedAndDueDateIfTypeOfDocumentSelectedIsOtherAndTransitionToStateIsHolding() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().typeOfDocumentAttached(OTHER).build())
            .application(Application.builder()
                .issueDate(LocalDate.of(2022, 1, 1))
                .stateToTransitionApplicationTo(Holding)
                .build())
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .build())
            .build();
        details.setData(caseData);

        when(holdingPeriodService.getDueDateFor(LocalDate.of(2022, 1, 1))).thenReturn(LocalDate.of(2022, 5, 22));

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState().name()).isEqualTo(Holding.name());
        assertThat(response.getData().getDueDate()).isEqualTo("2022-05-22");
    }

    @Test
    void shouldSetOnlyApplicant1ToOfflineIfSoleCaseAndStateToJSAwaitingLAD84SelectedAndIsJudicialSeparation() {
        setMockClock(clock);

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> scannedD84Document = ListValue
            .<ScannedDocument>builder()
            .id(FORM.getLabel())
            .value(
                ScannedDocument.builder()
                    .scannedDate(now(clock))
                    .fileName("D84.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder().build())
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .documents(
                CaseDocuments.builder()
                    .typeOfDocumentAttached(CO_D84)
                    .scannedDocuments(List.of(scannedD84Document))
                    .scannedDocumentNames(
                        DynamicList
                            .builder()
                            .value(
                                DynamicListElement
                                    .builder()
                                    .label("D84.pdf")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(JSAwaitingLA);
        assertThat(response.getData().getApplicant1().isApplicantOffline()).isTrue();
        assertThat(response.getData().getDocuments().getDocumentsGenerated()).hasSize(1);
        assertThat(response.getData().getConditionalOrder().getScannedD84Form()).isEqualTo(document);
        assertThat(response.getData().getConditionalOrder().getDateD84FormScanned()).isEqualTo(getExpectedLocalDateTime());

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldSetOnlyApplicant1ToOfflineIfSoleCaseAndStateAwaitingLegalAdvisorReferralIfD84SelectedAndNotJudicialSeparation() {
        setMockClock(clock);

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> scannedD84Document = ListValue
            .<ScannedDocument>builder()
            .id(FORM.getLabel())
            .value(
                ScannedDocument.builder()
                    .scannedDate(now(clock))
                    .fileName("D84.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder().build())
            .documents(
                CaseDocuments.builder()
                    .typeOfDocumentAttached(CO_D84)
                    .scannedDocuments(List.of(scannedD84Document))
                    .scannedDocumentNames(
                        DynamicList
                            .builder()
                            .value(
                                DynamicListElement
                                    .builder()
                                    .label("D84.pdf")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(AwaitingLegalAdvisorReferral);
        assertThat(response.getData().getApplicant1().isApplicantOffline()).isTrue();
        assertThat(response.getData().getDocuments().getDocumentsGenerated()).hasSize(1);
        assertThat(response.getData().getConditionalOrder().getScannedD84Form()).isEqualTo(document);
        assertThat(response.getData().getConditionalOrder().getDateD84FormScanned()).isEqualTo(getExpectedLocalDateTime());

        verify(notificationDispatcher)
            .send(app1AppliedForConditionalOrderNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSetBothApplicantsToOfflineIfJointCaseAndD84Selected() {
        setMockClock(clock);

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> scannedD84Document = ListValue
            .<ScannedDocument>builder()
            .id(FORM.getLabel())
            .value(
                ScannedDocument.builder()
                    .scannedDate(now(clock))
                    .fileName("D84.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder().build())
            .documents(
                CaseDocuments.builder()
                    .typeOfDocumentAttached(CO_D84)
                    .scannedDocuments(List.of(scannedD84Document))
                    .scannedDocumentNames(
                        DynamicList
                            .builder()
                            .value(
                                DynamicListElement
                                    .builder()
                                    .label("D84.pdf")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(AwaitingLegalAdvisorReferral);
        assertThat(response.getData().getApplicant1().isApplicantOffline()).isTrue();
        assertThat(response.getData().getApplicant2().isApplicantOffline()).isTrue();
        assertThat(response.getData().getDocuments().getDocumentsGenerated()).hasSize(1);
        assertThat(response.getData().getConditionalOrder().getScannedD84Form()).isEqualTo(document);
        assertThat(response.getData().getConditionalOrder().getDateD84FormScanned()).isEqualTo(getExpectedLocalDateTime());

        verify(notificationDispatcher)
            .send(app1AppliedForConditionalOrderNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldNotReclassifyDocumentAndNotSetScannedSubtypeReceivedToNullIfScannedDocIsD84AndSwitchToSoleSelected() {

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();

        final ListValue<ScannedDocument> scannedD84Document = ListValue
            .<ScannedDocument>builder()
            .id(FORM.getLabel())
            .value(
                ScannedDocument.builder()
                    .subtype("D84")
                    .fileName("D84.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final ListValue<DivorceDocument> doc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_APPLICATION)
                .documentFileName("d84.pdf")
                .documentComment("Reclassified scanned document")
                .documentDateAdded(getExpectedLocalDate())
                .documentLink(Document
                    .builder()
                    .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                    .filename("d84.pdf")
                    .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                    .build()
                )
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder().d84ApplicationType(SWITCH_TO_SOLE).build())
            .documents(
                CaseDocuments.builder()
                    .scannedSubtypeReceived(D84)
                    .documentsUploaded(singletonList(doc))
                    .scannedDocuments(List.of(scannedD84Document))
                    .typeOfDocumentAttached(CO_D84)
                    .build()
            )
            .build();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(AwaitingLegalAdvisorReferral);
        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNotNull();
        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isEqualTo(D84);

        verify(notificationDispatcher)
            .send(app1AppliedForConditionalOrderNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldNotReclassifyDocumentAndSetScannedSubtypeReceivedToNullIfScannedDocIsD84AndJointSelected() {

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();

        final ListValue<ScannedDocument> scannedD84Document = ListValue
            .<ScannedDocument>builder()
            .id(FORM.getLabel())
            .value(
                ScannedDocument.builder()
                    .subtype("D84")
                    .fileName("D84.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final ListValue<DivorceDocument> doc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_APPLICATION)
                .documentFileName("d84.pdf")
                .documentComment("Reclassified scanned document")
                .documentDateAdded(getExpectedLocalDate())
                .documentLink(Document
                    .builder()
                    .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                    .filename("d84.pdf")
                    .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                    .build()
                )
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder().d84ApplicationType(JOINT).build())
            .documents(
                CaseDocuments.builder()
                    .scannedSubtypeReceived(D84)
                    .documentsUploaded(singletonList(doc))
                    .scannedDocuments(List.of(scannedD84Document))
                    .typeOfDocumentAttached(CO_D84)
                    .build()
            )
            .build();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(AwaitingLegalAdvisorReferral);
        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNull();

        verify(notificationDispatcher)
            .send(app1AppliedForConditionalOrderNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSetOnlyApplicant1ToOfflineIfSoleCaseAndD36Selected() {
        setMockClock(clock);

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> scannedD36Document = ListValue
            .<ScannedDocument>builder()
            .id(FORM.getLabel())
            .value(
                ScannedDocument.builder()
                    .scannedDate(now(clock))
                    .fileName("D36.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder().build())
            .documents(
                CaseDocuments.builder()
                    .typeOfDocumentAttached(FO_D36)
                    .scannedDocuments(List.of(scannedD36Document))
                    .scannedDocumentNames(
                        DynamicList
                            .builder()
                            .value(
                                DynamicListElement
                                    .builder()
                                    .label("D36.pdf")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(FinalOrderRequested);
        assertThat(response.getData().getApplicant1().isApplicantOffline()).isTrue();
    }

    @Test
    void shouldSetOnlyApplicant2ToOfflineIfSoleCaseAndD36SelectedRespondentRequested() {
        setMockClock(clock);

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> scannedD36Document =  ListValue
            .<ScannedDocument>builder()
            .id(FORM.getLabel())
            .value(
                ScannedDocument.builder()
                    .scannedDate(now(clock))
                    .fileName("D36.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .finalOrder(FinalOrder.builder().d36WhoApplying(OfflineWhoApplying.APPLICANT_2).build())
            .documents(
                CaseDocuments.builder()
                    .typeOfDocumentAttached(FO_D36)
                    .scannedDocuments(List.of(scannedD36Document))
                    .scannedDocumentNames(
                        DynamicList
                            .builder()
                            .value(
                                DynamicListElement
                                    .builder()
                                    .label("D36.pdf")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getData().getApplicant1().isApplicantOffline()).isFalse();
        assertThat(response.getData().getApplicant2().isApplicantOffline()).isTrue();
    }

    @Test
    void shouldSetStateRespondentFinalOrderRequestedWhenSoleCaseAndD36SelectedRespondentRequested() {
        setMockClock(clock);

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> scannedD36Document =  ListValue
            .<ScannedDocument>builder()
            .id(FORM.getLabel())
            .value(
                ScannedDocument.builder()
                    .scannedDate(now(clock))
                    .fileName("D36.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .finalOrder(FinalOrder.builder().d36WhoApplying(OfflineWhoApplying.APPLICANT_2).build())
            .documents(
                CaseDocuments.builder()
                    .typeOfDocumentAttached(FO_D36)
                    .scannedDocuments(List.of(scannedD36Document))
                    .scannedDocumentNames(
                        DynamicList
                            .builder()
                            .value(
                                DynamicListElement
                                    .builder()
                                    .label("D36.pdf")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(RespondentFinalOrderRequested);
    }

    @Test
    void shouldSetBothApplicantsToOfflineIfJointCaseAndD36Selected() {
        setMockClock(clock);

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> scannedD36Document = ListValue
            .<ScannedDocument>builder()
            .id(FORM.getLabel())
            .value(
                ScannedDocument.builder()
                    .scannedDate(now(clock))
                    .fileName("D36.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder().build())
            .documents(
                CaseDocuments.builder()
                    .typeOfDocumentAttached(FO_D36)
                    .scannedDocuments(List.of(scannedD36Document))
                    .scannedDocumentNames(
                        DynamicList
                            .builder()
                            .value(
                                DynamicListElement
                                    .builder()
                                    .label("D36.pdf")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(FinalOrderRequested);
        assertThat(response.getData().getApplicant1().isApplicantOffline()).isTrue();
        assertThat(response.getData().getApplicant2().isApplicantOffline()).isTrue();
    }

    @Test
    void shouldNotReclassifyDocumentAndNotSetScannedSubtypeReceivedToNullIfScannedDocIsD36AndSwitchToSoleSelected() {

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();

        final ListValue<ScannedDocument> scannedD36Document = ListValue
            .<ScannedDocument>builder()
            .id(FORM.getLabel())
            .value(
                ScannedDocument.builder()
                    .subtype("D36")
                    .fileName("D36.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final ListValue<DivorceDocument> doc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(FINAL_ORDER_APPLICATION)
                .documentFileName("d36.pdf")
                .documentComment("Reclassified scanned document")
                .documentDateAdded(getExpectedLocalDate())
                .documentLink(Document
                    .builder()
                    .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                    .filename("d36.pdf")
                    .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                    .build()
                )
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().build())
            .finalOrder(FinalOrder.builder().d36ApplicationType(SWITCH_TO_SOLE).build())
            .documents(
                CaseDocuments.builder()
                    .scannedSubtypeReceived(D36)
                    .documentsUploaded(singletonList(doc))
                    .scannedDocuments(List.of(scannedD36Document))
                    .typeOfDocumentAttached(FO_D36)
                    .build()
            )
            .build();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(FinalOrderRequested);
        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNotNull();
        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isEqualTo(D36);
    }

    @Test
    void shouldNotReclassifyDocumentAndSetScannedSubtypeReceivedToNullIfScannedDocIsD36AndJointSelected() {

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();

        final ListValue<ScannedDocument> scannedD36Document = ListValue
            .<ScannedDocument>builder()
            .id(FORM.getLabel())
            .value(
                ScannedDocument.builder()
                    .subtype("D36")
                    .fileName("D36.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        final ListValue<DivorceDocument> doc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(FINAL_ORDER_APPLICATION)
                .documentFileName("d36.pdf")
                .documentComment("Reclassified scanned document")
                .documentDateAdded(getExpectedLocalDate())
                .documentLink(Document
                    .builder()
                    .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                    .filename("d36.pdf")
                    .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                    .build()
                )
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder().build())
            .applicant2(Applicant.builder().build())
            .finalOrder(FinalOrder.builder().d36ApplicationType(JOINT).build())
            .documents(
                CaseDocuments.builder()
                    .scannedSubtypeReceived(D36)
                    .documentsUploaded(singletonList(doc))
                    .scannedDocuments(List.of(scannedD36Document))
                    .typeOfDocumentAttached(FO_D36)
                    .build()
            )
            .build();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(FinalOrderRequested);
        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNull();
    }

    @ParameterizedTest
    @CsvSource({"D10,AOS_D10", "D84,CO_D84", "D36,FO_D36", ","})
    void shouldSetTypeOfDocumentAttachedFromScannedDocumentSubtype(final ScannedDocumentSubtypes subtype,
                                                                   final OfflineDocumentReceived typeOfDocumentAttached) {
        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .scannedSubtypeReceived(subtype)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerOfflineDocumentVerified.aboutToStart(details);

        assertThat(response.getData().getDocuments().getTypeOfDocumentAttached()).isEqualTo(typeOfDocumentAttached);
    }

    @ParameterizedTest
    @CsvSource({"AOS_D10", "CO_D84", "FO_D36"})
    void shouldNotOverwriteTypeOfDocumentAttachedIfScannedDocumentSubtypeNull(final OfflineDocumentReceived typeOfDocumentAttached) {
        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .typeOfDocumentAttached(typeOfDocumentAttached)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerOfflineDocumentVerified.aboutToStart(details);

        assertThat(response.getData().getDocuments().getTypeOfDocumentAttached()).isEqualTo(typeOfDocumentAttached);
    }

    @Test
    void shouldSetDynamicListWithScannedDocumentNamesForAllTheScannedDocuments() {
        final ListValue<ScannedDocument> doc1 = scannedDocument("doc1.pdf");
        final ListValue<ScannedDocument> doc2 = scannedDocument("doc2.pdf");

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .scannedDocuments(List.of(doc1, doc2))
                .build())
            .build();

        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerOfflineDocumentVerified.aboutToStart(details);

        assertThat(response.getData().getDocuments().getScannedDocumentNames().getListItems())
            .extracting("label")
            .contains("doc1.pdf", "doc2.pdf");
    }

    @Test
    void shouldInvokeGeneralReferralServiceFO_D36() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().typeOfDocumentAttached(FO_D36).build())
            .build();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getDocuments().setScannedSubtypeReceived(D36);
        details.setData(caseData);

        caseworkerOfflineDocumentVerified.submitted(details, details);

        verify(generalReferralService).caseWorkerGeneralReferral(details);
    }

    @Test
    void shouldInvokeGeneralReferralServiceD36() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .documents(CaseDocuments.builder().typeOfDocumentAttached(FO_D36).scannedSubtypeReceived(D36).build())
            .build();
        details.setData(caseData);

        caseworkerOfflineDocumentVerified.submitted(details, details);

        verify(generalReferralService).caseWorkerGeneralReferral(details);
    }


    @Test
    void shouldSendOfflineNotifications() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .documents(CaseDocuments.builder().typeOfDocumentAttached(AOS_D10).scannedSubtypeReceived(D10).build())
            .application(Application.builder()
                .issueDate(LocalDate.of(2022, 1, 1))
                .stateToTransitionApplicationTo(Holding)
                .build())
            .build();
        details.setData(caseData);

        when(submitAosService.submitOfflineAos(any())).thenReturn(details);

        caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        verify(submitAosService).submitAosNotifications(details);
        verifyNoMoreInteractions(submitAosService);
    }

    @Test
    void shouldNotSetDynamicListWithScannedDocumentNamesIfScannedSubtypeReceivedIsPopulated() {
        final ListValue<ScannedDocument> doc1 = scannedDocument("doc1.pdf");
        final ListValue<ScannedDocument> doc2 = scannedDocument("doc2.pdf");

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .scannedSubtypeReceived(D36)
                .scannedDocuments(List.of(doc1, doc2))
                .build())
            .build();

        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerOfflineDocumentVerified.aboutToStart(details);

        assertThat(response.getData().getDocuments().getScannedDocumentNames()).isNull();
    }

    @Test
    void shouldTriggerSwitchToSoleEventIfD84AndSwitchToSoleSelected() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .documents(CaseDocuments.builder()
                .typeOfDocumentAttached(CO_D84)
                .scannedSubtypeReceived(D84)
                .build())
            .conditionalOrder(ConditionalOrder.builder().d84ApplicationType(SWITCH_TO_SOLE).build())
            .build();

        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().build();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final var userDetails = UserInfo.builder().uid(CASEWORKER_USER_ID).build();
        final User user = new User(CASEWORKER_AUTH_TOKEN, userDetails);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        caseworkerOfflineDocumentVerified.submitted(details, details);
        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SWITCH_TO_SOLE_CO, user, TEST_SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldNotTriggerSwitchToSoleEventIfD84AndSwitchToSoleNotSelected() {
        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().build())
            .conditionalOrder(ConditionalOrder.builder().build())
            .build();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().build();
        details.setData(caseData);

        caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldTriggerSwitchToSoleEventIfD36AndSwitchToSoleSelected() {
        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .typeOfDocumentAttached(FO_D36)
                .build())
            .finalOrder(FinalOrder.builder().d36ApplicationType(SWITCH_TO_SOLE).build())
            .build();
        caseData.getDocuments().setScannedSubtypeReceived(D36);
        caseData.setApplicationType(JOINT_APPLICATION);

        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().build();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final var userDetails = UserInfo.builder().uid(CASEWORKER_USER_ID).build();
        final User user = new User(CASEWORKER_AUTH_TOKEN, userDetails);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        caseworkerOfflineDocumentVerified.submitted(details, details);

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SWITCH_TO_SOLE_FO_OFFLINE, user, TEST_SERVICE_AUTH_TOKEN);
        verifyNoInteractions(generalReferralService);
    }

    @Test
    void shouldNotTriggerSwitchToSoleEventIfD36AndSwitchToSoleNotSelected() {
        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .typeOfDocumentAttached(FO_D36)
                .build())
            .finalOrder(FinalOrder.builder().build())
            .build();
        caseData.getDocuments().setScannedSubtypeReceived(D36);
        caseData.setApplicationType(JOINT_APPLICATION);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().build();
        details.setData(caseData);

        caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        verifyNoInteractions(ccdUpdateService);
    }

    private ListValue<ScannedDocument> scannedDocument(String filename) {
        return ListValue.<ScannedDocument>builder()
            .value(ScannedDocument.builder()
                .url(Document.builder()
                    .filename(filename)
                    .url("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d")
                    .binaryUrl("http://localhost:8080/f62d42fd-a5f0-43ff-874b-d1666c1bf00d/binary")
                    .build()
                )
                .fileName(filename)
                .type(ScannedDocumentType.OTHER)
                .subtype("aos")
                .build()
            ).build();
    }

    @Test
    void shouldNotTriggerSwitchToSoleEventIfD36OrFOD36AndNotSwitchToSoleSelected() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
                .documents(CaseDocuments.builder()
                .typeOfDocumentAttached(FO_D36)
                    .scannedSubtypeReceived(D36)
                .build())
            .finalOrder(FinalOrder.builder().d36ApplicationType(JOINT).build())
            .build();

        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().build();
        details.setData(caseData);

        caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSendOfflineNotificationsForConditionalOrderWhenJudicialSeparation() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
                .documents(CaseDocuments.builder().typeOfDocumentAttached(CO_D84).build())
            .conditionalOrder(ConditionalOrder.builder().d84ApplicationType(SWITCH_TO_SOLE).build())
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .application(Application.builder()
                .issueDate(LocalDate.of(2022, 1, 1))
                .stateToTransitionApplicationTo(Holding)
                .build())
            .build();
        caseData.getDocuments().setScannedSubtypeReceived(D84);
        caseData.setApplicationType(JOINT_APPLICATION);
        details.setData(caseData);

        caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        verifyNoInteractions(notificationDispatcher);
    }
}
