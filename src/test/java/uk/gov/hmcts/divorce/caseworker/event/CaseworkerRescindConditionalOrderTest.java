package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralConsiderationComplete;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerRescindConditionalOrderTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private CaseworkerRescindConditionalOrder caseworkerRescindConditionalOrder;

    @Test
    void shouldUnlinkFromBulkCaseIfStateIsAwaitingPronouncement() {

        setMockClock(clock);

        final var caseData = caseData();

        final List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        final ListValue<DivorceDocument> coGrantedDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED)
                .build())
            .build();
        documentsGenerated.add(coGrantedDoc);

        caseData.setBulkListCaseReferenceLink(CaseLink.builder().caseReference("1234-1234-1234-1234").build());
        caseData.setConditionalOrder(
            ConditionalOrder.builder()
                .conditionalOrderGrantedDocument(coGrantedDoc.getValue())
                .build()
        );
        caseData.setDocuments(
            CaseDocuments.builder()
                .documentsGenerated(documentsGenerated)
                .build()
        );

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingPronouncement);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRescindConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getBulkListCaseReferenceLink()).isNull();
    }

    @Test
    void shouldRemoveConditionalOrderDocumentsFromCaseDataAndSetRescindedDateAndTime() {

        setMockClock(clock);

        final var caseData = caseData();

        final List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        final ListValue<DivorceDocument> coGrantedDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED)
                .build())
            .build();
        documentsGenerated.add(coGrantedDoc);

        caseData.setConditionalOrder(
            ConditionalOrder.builder()
                .conditionalOrderGrantedDocument(coGrantedDoc.getValue())
                .build()
        );
        caseData.setDocuments(
            CaseDocuments.builder()
                .documentsGenerated(documentsGenerated)
                .build()
        );

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(GeneralConsiderationComplete);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRescindConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getRescindedDate()).isEqualTo(getExpectedLocalDate());
        assertThat(response.getData().getConditionalOrder().getConditionalOrderGrantedDocument()).isNull();
        assertThat(response.getData().getDocuments().getDocumentsGenerated()).isEmpty();
    }
}
