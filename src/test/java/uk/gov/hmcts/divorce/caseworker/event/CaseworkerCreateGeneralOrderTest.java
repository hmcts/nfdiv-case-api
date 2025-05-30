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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.caseworker.event.page.CreateGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerCreateGeneralOrder.CASEWORKER_CREATE_GENERAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addScannedDocument;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceGeneralOrderListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getGeneralOrder;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getGeneralOrderDocument;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getScannedGeneralOrderDocument;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.setScannedDocumentNames;

@ExtendWith(MockitoExtension.class)
class CaseworkerCreateGeneralOrderTest {
    private static final String LIST_VALUE_ID_1 = "1234";
    private static final String LIST_VALUE_ID_2 = "4567";

    @Mock
    private CreateGeneralOrder createGeneralOrder;

    @Mock
    private DocumentIdProvider documentIdProvider;

    @InjectMocks
    private CaseworkerCreateGeneralOrder caseworkerCreateGeneralOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = ConfigTestUtil.createCaseDataConfigBuilder();

        caseworkerCreateGeneralOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .containsExactly(CASEWORKER_CREATE_GENERAL_ORDER);
    }

    @Test
    void shouldReturnListOfScannedDocumentNames() {
        final CaseData caseData = caseData();

        ScannedDocument scannedDocument = getScannedGeneralOrderDocument();
        addScannedDocument(caseData, scannedDocument);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartResponse = caseworkerCreateGeneralOrder.aboutToStart(details);

        final CaseData expectedData = caseData();
        addScannedDocument(expectedData, scannedDocument);
        setScannedDocumentNames(expectedData);

        assertThat(aboutToStartResponse.getData().getDocuments().getScannedDocumentNames().getListItems().get(0).getLabel())
            .isEqualTo(expectedData.getDocuments().getScannedDocumentNames().getListItems().get(0).getLabel());
    }

    @Test
    void shouldSetGeneralOrderDocumentsWhenThereIsNoExistingGeneralOrder() {
        final CaseData caseData = caseData();

        final Document generalOrderDocument = getGeneralOrderDocument();

        caseData.setGeneralOrder(getGeneralOrder(generalOrderDocument));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(documentIdProvider.documentId()).thenReturn(LIST_VALUE_ID_1);

        AboutToStartOrSubmitResponse<CaseData, State> submitResponse = caseworkerCreateGeneralOrder.aboutToSubmit(details, details);

        assertThat(submitResponse.getData().getGeneralOrder()).isNull();
        assertThat(submitResponse.getData().getGeneralOrders())
            .containsExactly(getDivorceGeneralOrderListValue(generalOrderDocument, LIST_VALUE_ID_1));
    }

    @Test
    void shouldAddLatestGeneralOrderDocumentToTopWhenThereIsExistingGeneralOrder() {
        final CaseData caseData = caseData();

        Document generalOrderDocument1 = getGeneralOrderDocument();

        Document generalOrderDocument2 = getGeneralOrderDocument();

        caseData.setGeneralOrder(getGeneralOrder(generalOrderDocument2));

        final List<ListValue<DivorceGeneralOrder>> generalOrders = new ArrayList<>();
        generalOrders.add(getDivorceGeneralOrderListValue(generalOrderDocument1, LIST_VALUE_ID_1));
        caseData.setGeneralOrders(generalOrders);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(documentIdProvider.documentId()).thenReturn(LIST_VALUE_ID_2);

        AboutToStartOrSubmitResponse<CaseData, State> submitResponse = caseworkerCreateGeneralOrder.aboutToSubmit(details, details);

        assertThat(submitResponse.getData().getGeneralOrder()).isNull();
        assertThat(submitResponse.getData().getGeneralOrders()).containsExactly(
            getDivorceGeneralOrderListValue(generalOrderDocument2, LIST_VALUE_ID_2),
            getDivorceGeneralOrderListValue(generalOrderDocument1, LIST_VALUE_ID_1)
        );
    }

    @Test
    void shouldSetGeneralOrderScannedDocument() {
        final CaseData caseData = caseData();

        final Document generalOrder = getGeneralOrderDocument();

        final ScannedDocument scannedGeneralOrderDocument = getScannedGeneralOrderDocument();

        caseData.setGeneralOrder(getGeneralOrder(scannedGeneralOrderDocument));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        when(documentIdProvider.documentId()).thenReturn(LIST_VALUE_ID_1);

        AboutToStartOrSubmitResponse<CaseData, State> submitResponse = caseworkerCreateGeneralOrder.aboutToSubmit(details, details);

        assertThat(submitResponse.getData().getGeneralOrder()).isNull();
        assertThat(submitResponse.getData().getGeneralOrders())
            .containsExactly(getDivorceGeneralOrderListValue(generalOrder, LIST_VALUE_ID_1));
    }
}
