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
import uk.gov.hmcts.divorce.common.service.SubmitAosService;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerOfflineDocumentVerified.CASEWORKER_OFFLINE_DOCUMENT_VERIFIED;
import static uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService.OfflineDocumentReceived.AOS_D10;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAmendedApplication;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CaseworkerOfflineDocumentVerifiedTest {

    @Mock
    private SubmitAosService submitAosService;

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
    void shouldSetStateToHoldingAndReclassifyAosScannedDocumentToRespondentAnswersIfD10DocumentSelected() {
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
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .typeOfDocumentAttached(AOS_D10)
                .howToRespondApplication(DISPUTE_DIVORCE)
                .build())
            .build();
        details.setData(caseData);

        final CaseDetails<CaseData, State> updatedDetails = new CaseDetails<>();
        updatedDetails.setData(CaseData.builder()
            .applicant2(Applicant.builder()
                .build())
            .documents(
                CaseDocuments.builder()
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
            .build());
        updatedDetails.setState(Holding);

        when(submitAosService.submitOfflineAos(details)).thenReturn(updatedDetails);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        verify(submitAosService).submitOfflineAos(details);
        assertThat(response.getState().getName()).isEqualTo(Holding.getName());
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
    }

    @Test
    void shouldSetStateToUserValueProvidedIfTypeOfDocumentSelectedIsOther() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .stateToTransitionApplicationTo(AwaitingAmendedApplication)
                .build())
            .acknowledgementOfService(AcknowledgementOfService.builder()
                .typeOfDocumentAttached(AcknowledgementOfService.OfflineDocumentReceived.OTHER)
                .build())
            .build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerOfflineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState().getName()).isEqualTo(AwaitingAmendedApplication.getName());
    }

    @Test
    void shouldSetDynamicListWithScannedDocumentNamesForAllTheScannedDocuments() {
        final ListValue<ScannedDocument> doc1 = ListValue.<ScannedDocument>builder()
            .value(
                ScannedDocument
                    .builder()
                    .fileName("doc1.pdf")
                    .type(ScannedDocumentType.OTHER)
                    .subtype("aos")
                    .build()
            )
            .build();

        final ListValue<ScannedDocument> doc2 = ListValue.<ScannedDocument>builder()
            .value(
                ScannedDocument
                    .builder()
                    .fileName("doc2.pdf")
                    .type(ScannedDocumentType.OTHER)
                    .build()
            )
            .build();

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
}
