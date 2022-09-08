package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.common.service.SubmitAosService;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.FORM;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerOfflineDocumentVerified.CASEWORKER_OFFLINE_DOCUMENT_VERIFIED;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSwitchedToSoleCo.SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.AOS_D10;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.CO_D84;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder.D84ApplicationType.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAmendedApplication;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
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
public class CaseworkerOfflineDocumentVerifiedTest {

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

        verify(submitAosService).submitOfflineAos(details);
        assertThat(response.getState().name()).isEqualTo(Holding.name());
        assertThat(response.getData().getApplicant2().getOffline()).isEqualTo(YES);

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

        verify(submitAosService).submitOfflineAos(details);
        assertThat(response.getState().name()).isEqualTo(Holding.name());
        assertThat(response.getData().getApplicant2().getOffline()).isEqualTo(YES);


        assertThat(response.getData().getDocuments().getDocumentsUploaded()).isNull();
    }

    @Test
    void shouldSetStateToUserValueProvidedIfTypeOfDocumentSelectedIsOther() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().typeOfDocumentAttached(OTHER).build())
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
    }

    @Test
    void shouldSetStateToUserValueProvidedAndDueDateIfTypeOfDocumentSelectedIsOtherAndTransitionToStateIsHolding() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().typeOfDocumentAttached(OTHER).build())
            .application(Application.builder()
                .issueDate(LocalDate.of(2022, 01, 01))
                .stateToTransitionApplicationTo(Holding)
                .build())
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .build())
            .build();
        details.setData(caseData);

        when(holdingPeriodService.getDueDateFor(LocalDate.of(2022, 01, 01))).thenReturn(LocalDate.of(2022, 05, 22));

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState().name()).isEqualTo(Holding.name());
        assertThat(response.getData().getDueDate()).isEqualTo("2022-05-22");
    }

    @Test
    void shouldSetOnlyApplicant1ToOfflineIfSoleCaseAndD84Selected() {
        setMockClock(clock);

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> scannedD84Document =  ListValue
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
        assertThat(response.getData().getApplicant1().isOffline()).isTrue();
        assertThat(response.getData().getDocuments().getDocumentsGenerated().size()).isEqualTo(1);
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
        final ListValue<ScannedDocument> scannedD84Document =  ListValue
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
        assertThat(response.getData().getApplicant1().isOffline()).isTrue();
        assertThat(response.getData().getApplicant2().isOffline()).isTrue();
        assertThat(response.getData().getDocuments().getDocumentsGenerated().size()).isEqualTo(1);
        assertThat(response.getData().getConditionalOrder().getScannedD84Form()).isEqualTo(document);
        assertThat(response.getData().getConditionalOrder().getDateD84FormScanned()).isEqualTo(getExpectedLocalDateTime());

        verify(notificationDispatcher)
            .send(app1AppliedForConditionalOrderNotification, caseData, TEST_CASE_ID);
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
    void shouldTriggerSwitchToSoleEventIfD84AndSwitchToSoleSelected() {
        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .typeOfDocumentAttached(CO_D84)
                .build())
            .conditionalOrder(ConditionalOrder.builder().d84ApplicationType(SWITCH_TO_SOLE).build())
            .build();

        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().build();
        details.setData(caseData);

        final UserDetails userDetails = UserDetails.builder().id(CASEWORKER_USER_ID).build();
        final User user = new User(CASEWORKER_AUTH_TOKEN, userDetails);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        caseworkerOfflineDocumentVerified.submitted(details, details);

        verify(ccdUpdateService).submitEvent(details, SWITCH_TO_SOLE_CO, user, TEST_SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldNotTriggerSwitchToSoleEventIfD84AndSwitchToSoleNotSelected() {
        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().build())
            .conditionalOrder(ConditionalOrder.builder().build())
            .build();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().build();
        details.setData(caseData);

        caseworkerOfflineDocumentVerified.submitted(details, details);

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
}
