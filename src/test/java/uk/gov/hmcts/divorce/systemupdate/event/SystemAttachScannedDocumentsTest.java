package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.api.Event.ATTACH_SCANNED_DOCS;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D10;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D36;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D84;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.valueOf;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.builder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.scannedDocuments;

@ExtendWith(SpringExtension.class)
public class SystemAttachScannedDocumentsTest {

    private static final List<ListValue<ScannedDocument>> BEFORE_SCANNED_DOCUMENTS =
        scannedDocuments(asList(D10.getLabel(), D84.getLabel()));

    @Mock
    private Clock clock;

    @InjectMocks
    private SystemAttachScannedDocuments systemAttachScannedDocuments;

    private CaseDetails<CaseData, State> beforeDetails;

    @BeforeEach
    void setUp() {
        setMockClock(clock);
        beforeDetails = getCaseDetails(BEFORE_SCANNED_DOCUMENTS);
    }

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
        beforeDetails.getData().getDocuments().setScannedSubtypeReceived(D36);

        final AboutToStartOrSubmitResponse<CaseData, State> response = systemAttachScannedDocuments.aboutToStart(beforeDetails);

        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNull();
    }

    @Test
    void shouldSetPreviousState() {
        beforeDetails.setState(AwaitingApplicant2Response);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemAttachScannedDocuments.aboutToSubmit(beforeDetails, beforeDetails);

        assertThat(response.getData().getApplication().getPreviousState()).isEqualTo(AwaitingApplicant2Response);
    }


    @ParameterizedTest
    @ValueSource(strings = {"D10", "D84", "D36"})
    void shouldReclassifyScannedDocumentAndAddToDocumentsUploadedIfSubtypeIsValid(String subtype) {
        final List<ListValue<ScannedDocument>> afterScannedDocuments = scannedDocuments(singletonList(subtype));
        afterScannedDocuments.get(0).getValue().setDeliveryDate(now());
        afterScannedDocuments.addAll(BEFORE_SCANNED_DOCUMENTS);
        final CaseDetails<CaseData, State> details = getCaseDetails(afterScannedDocuments);

        AboutToStartOrSubmitResponse<CaseData, State> response = systemAttachScannedDocuments.aboutToSubmit(details, beforeDetails);

        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isEqualTo(valueOf(subtype));
        assertThat(response.getData().getDocuments().getDocumentsUploaded()).hasSize(1);
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"D36N", "test", ""})
    void shouldNotSetScannedSubtypeReceivedOrReclassifyDocumentIfScannedDocumentSubtypeIsNotSupported(String subtype) {
        final List<ListValue<ScannedDocument>> afterScannedDocuments = scannedDocuments(singletonList(subtype));
        afterScannedDocuments.get(0).getValue().setDeliveryDate(now());
        afterScannedDocuments.addAll(BEFORE_SCANNED_DOCUMENTS);
        final CaseDetails<CaseData, State> details = getCaseDetails(afterScannedDocuments);

        AboutToStartOrSubmitResponse<CaseData, State> response = systemAttachScannedDocuments.aboutToSubmit(details, beforeDetails);

        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNull();
        assertThat(response.getData().getDocuments().getDocumentsUploaded()).isNull();
    }

    @Test
    void shouldIgnoreCcdIdsWhenComparingDocuments() {
        final List<ListValue<ScannedDocument>> scannedDocuments = scannedDocuments(asList(D10.getLabel(), D84.getLabel()));
        final CaseDetails<CaseData, State> details = getCaseDetails(scannedDocuments);
        AboutToStartOrSubmitResponse<CaseData, State> response = systemAttachScannedDocuments.aboutToSubmit(details, beforeDetails);
        assertThat(response.getData().getDocuments().getScannedSubtypeReceived()).isNull();
        assertThat(response.getData().getDocuments().getDocumentsUploaded()).isNull();
    }

    private CaseDetails<CaseData, State> getCaseDetails(final List<ListValue<ScannedDocument>> scannedDocuments) {
        return CaseDetails.<CaseData, State>builder()
            .data(CaseData.builder()
                .documents(builder()
                    .scannedDocuments(scannedDocuments)
                    .build())
                .build())
            .build();
    }
}
