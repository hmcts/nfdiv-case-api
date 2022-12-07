package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Event.ATTACH_SCANNED_DOCS;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.FORM;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D10;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D36;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D36N;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D84;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class SystemAttachScannedDocumentsTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private SystemAttachScannedDocuments systemAttachScannedDocuments;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemAttachScannedDocuments.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(ATTACH_SCANNED_DOCS);
    }

    @Test
    void shouldSetScannedSubtypeReceivedToNullInAboutToStartCallback() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getDocuments().setScannedSubtypeReceived(D36);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemAttachScannedDocuments.aboutToStart(details);

        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNull();
    }

    @Test
    void shouldSetPreviousState() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);
        details.setState(AwaitingApplicant2Response);

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemAttachScannedDocuments.aboutToSubmit(details, details);

        assertThat(response.getData().getApplication().getPreviousState()).isEqualTo(AwaitingApplicant2Response);
    }


    @ParameterizedTest
    @ValueSource(strings = {"D10", "D84", "D36"})
    void shouldReclassifyScannedDocumentAndAddToDocumentsUploadedIfSubtypeIsValid(String subtype) {

        setMockClock(clock);

        ReflectionTestUtils.setField(systemAttachScannedDocuments, "qrCodeReadingEnabled", true);

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> d36Document = ListValue
            .<ScannedDocument>builder()
            .id(D36.getLabel())
            .value(
                ScannedDocument.builder()
                    .subtype(subtype)
                    .fileName(subtype + ".pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        CaseData beforeCaseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .scannedDocuments(getScannedDocuments())
                    .build()
            )
            .build();
        beforeDetails.setData(beforeCaseData);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .scannedDocuments(getScannedDocuments())
                    .build()
            )
            .build();
        caseData.getDocuments().getScannedDocuments().add(d36Document);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            systemAttachScannedDocuments.aboutToSubmit(details, beforeDetails);

        assertThat(response.getData().getDocuments().getScannedSubtypeReceived())
            .isEqualTo(CaseDocuments.ScannedDocumentSubtypes.valueOf(subtype));
        assertThat(response.getData().getDocuments().getDocumentsUploaded()).hasSize(1);
    }

    @Test
    void shouldNotSetScannedSubtypeReceivedOrReclassifyDocumentIfScannedDocumentSubtypeIsNotSupported() {
        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> d36NDocument = ListValue
            .<ScannedDocument>builder()
            .id(D36N.getLabel())
            .value(
                ScannedDocument.builder()
                    .subtype("D36N")
                    .fileName("D36N.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        CaseData beforeCaseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .scannedDocuments(getScannedDocuments())
                    .build()
            )
            .build();
        beforeDetails.setData(beforeCaseData);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .scannedDocuments(getScannedDocuments())
                    .build()
            )
            .build();
        caseData.getDocuments().getScannedDocuments().add(d36NDocument);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            systemAttachScannedDocuments.aboutToSubmit(details, beforeDetails);

        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNull();
        assertThat(response.getData().getDocuments().getDocumentsUploaded()).isNull();
    }

    @Test
    void shouldNotSetScannedSubtypeReceivedOrReclassifyDocumentIfMostRecentScannedDocumentSubtypesIsInvalid() {
        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> invalidDocument = ListValue
            .<ScannedDocument>builder()
            .id(FORM.getLabel())
            .value(
                ScannedDocument.builder()
                    .subtype("test")
                    .fileName("test.pdf")
                    .type(FORM)
                    .url(document)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        CaseData beforeCaseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .scannedDocuments(getScannedDocuments())
                    .build()
            )
            .build();
        beforeDetails.setData(beforeCaseData);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .scannedDocuments(getScannedDocuments())
                    .build()
            )
            .build();
        caseData.getDocuments().getScannedDocuments().add(invalidDocument);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            systemAttachScannedDocuments.aboutToSubmit(details, beforeDetails);

        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNull();
        assertThat(response.getData().getDocuments().getDocumentsUploaded()).isNull();
    }

    @Test
    void shouldSkipReclassifyDocumentIfScannedDocumentSubtypesIsNotPresent() {
        setMockClock(clock);

        ReflectionTestUtils.setField(systemAttachScannedDocuments, "qrCodeReadingEnabled", true);

        final Document document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> otherDocument = ListValue
            .<ScannedDocument>builder()
            .id(OTHER.getLabel())
            .value(
                ScannedDocument.builder()
                    .fileName("otherdoc.pdf")
                    .type(OTHER)
                    .url(document)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        CaseData beforeCaseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .scannedDocuments(getScannedDocuments())
                    .build()
            )
            .build();
        beforeDetails.setData(beforeCaseData);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .documents(
                CaseDocuments.builder()
                    .scannedDocuments(getScannedDocuments())
                    .build()
            )
            .build();
        caseData.getDocuments().getScannedDocuments().add(otherDocument);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            systemAttachScannedDocuments.aboutToSubmit(details, beforeDetails);

        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNull();
        assertThat(response.getData().getDocuments().getDocumentsUploaded()).isNull();
    }

    private List<ListValue<ScannedDocument>> getScannedDocuments() {

        final Document d10Document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> scannedD10Document = ListValue
            .<ScannedDocument>builder()
            .id(D10.getLabel())
            .value(
                ScannedDocument.builder()
                    .subtype("d10")
                    .fileName("D10.pdf")
                    .type(FORM)
                    .url(d10Document)
                    .build()
            )
            .build();

        final Document d84Document = Document.builder()
            .url("/filename")
            .binaryUrl("/filename/binary")
            .filename("filename")
            .build();
        final ListValue<ScannedDocument> scannedD84Document = ListValue
            .<ScannedDocument>builder()
            .id(D84.getLabel())
            .value(
                ScannedDocument.builder()
                    .subtype("d84")
                    .fileName("D84.pdf")
                    .type(FORM)
                    .url(d84Document)
                    .build()
            )
            .build();

        List<ListValue<ScannedDocument>> documents = new ArrayList<>();
        documents.add(scannedD10Document);
        documents.add(scannedD84Document);

        return documents;
    }
}
