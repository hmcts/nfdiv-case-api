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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceGeneralOrderListValue;

@ExtendWith(MockitoExtension.class)
public class CaseworkerRemoveGeneralOrderTest {

    @Mock
    private DocumentRemovalService documentRemovalService;

    @Mock
    private DocumentIdProvider documentIdProvider;

    @InjectMocks
    private CaseworkerRemoveGeneralOrder caseworkerRemoveGO;

    private static final String LIST_VALUE_ID_1 = "1234";
    private static final String LIST_VALUE_ID_2 = "4567";

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRemoveGO.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CaseworkerRemoveGeneralOrder.CASEWORKER_REMOVE_GO);
    }

    @Test
    void shouldRemoveDocumentForDeletedGeneralOrder() {

        final CaseData caseData1 = caseData();
        final CaseData caseData2 = caseData();

        String documentUrl = "http://localhost:8080/4567";

        Document generalOrderDoc1 = new Document(
            documentUrl,
            "generalOrder2020-07-16 11:10:34.pdf",
            documentUrl + "/binary"
        );

        final List<ListValue<DivorceGeneralOrder>> generalOrders1 = new ArrayList<>();
        generalOrders1.add(getDivorceGeneralOrderListValue(generalOrderDoc1, LIST_VALUE_ID_1));
        caseData1.setGeneralOrders(generalOrders1);


        final CaseDetails<CaseData, State> afterdetails = new CaseDetails<>();
        afterdetails.setData(caseData1);
        afterdetails.setId(TEST_CASE_ID);

        Document generalOrderDoc2 = new Document(
            documentUrl,
            "generalOrder2021-07-16 11:10:34.pdf",
            documentUrl + "/binary"
        );

        final List<ListValue<DivorceGeneralOrder>> generalOrders2 = new ArrayList<>();
        generalOrders2.add(getDivorceGeneralOrderListValue(generalOrderDoc1, LIST_VALUE_ID_1));
        generalOrders2.add(getDivorceGeneralOrderListValue(generalOrderDoc2, LIST_VALUE_ID_2));
        caseData2.setGeneralOrders(generalOrders2);

        final CaseDetails<CaseData, State> beforedetails = new CaseDetails<>();
        beforedetails.setData(caseData2);
        beforedetails.setId(TEST_CASE_ID);

        List<ListValue<DivorceDocument>> documentsToDelete = new ArrayList<>();
        DivorceDocument doc = getDivorceGeneralOrderListValue(generalOrderDoc2, LIST_VALUE_ID_2)
            .getValue().getGeneralOrderDocument();

        documentsToDelete = List.of(ListValue.<DivorceDocument>builder()
            .id(LIST_VALUE_ID_2)
            .value(doc).build());

        when(documentIdProvider.documentId()).thenReturn(LIST_VALUE_ID_2);

        AboutToStartOrSubmitResponse<CaseData, State> submitResponse = caseworkerRemoveGO.aboutToSubmit(afterdetails, beforedetails);

        verify(documentRemovalService).deleteDocument(documentsToDelete);
        assertThat(submitResponse.getData().getGeneralOrders().size() == 1);
    }

    @Test
    void shouldNotCallRemoveDocumentIfGeneralOrderListIsSame() {

        final CaseData caseData = caseData();

        String documentUrl = "http://localhost:8080/4567";

        Document generalOrderDoc1 = new Document(
            documentUrl,
            "generalOrder2020-07-16 11:10:34.pdf",
            documentUrl + "/binary"
        );

        final List<ListValue<DivorceGeneralOrder>> generalOrders = new ArrayList<>();
        generalOrders.add(getDivorceGeneralOrderListValue(generalOrderDoc1, LIST_VALUE_ID_1));

        Document generalOrderDoc2 = new Document(
            documentUrl,
            "generalOrder2021-07-16 11:10:34.pdf",
            documentUrl + "/binary"
        );

        generalOrders.add(getDivorceGeneralOrderListValue(generalOrderDoc2, LIST_VALUE_ID_2));

        caseData.setGeneralOrders(generalOrders);


        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> submitResponse = caseworkerRemoveGO.aboutToSubmit(details, details);

        verifyNoInteractions(documentRemovalService);
        assertThat(submitResponse.getData().getGeneralOrders().size() == 2);
    }

    @Test
    void shouldRemoveAllDocumentsIfAllGeneralOrdersAreDeleted() {

        final CaseData caseData1 = caseData();
        final CaseData caseData2 = caseData();

        caseData1.setGeneralOrders(null);
        final CaseDetails<CaseData, State> afterdetails = new CaseDetails<>();
        afterdetails.setData(caseData1);
        afterdetails.setId(TEST_CASE_ID);

        String documentUrl = "http://localhost:8080/4567";
        Document generalOrderDoc1 = new Document(
            documentUrl,
            "generalOrder2020-07-16 11:10:34.pdf",
            documentUrl + "/binary"
        );

        final List<ListValue<DivorceGeneralOrder>> generalOrders = new ArrayList<>();
        generalOrders.add(getDivorceGeneralOrderListValue(generalOrderDoc1, LIST_VALUE_ID_1));

        Document generalOrderDoc2 = new Document(
            documentUrl,
            "generalOrder2021-07-16 11:10:34.pdf",
            documentUrl + "/binary"
        );
        generalOrders.add(getDivorceGeneralOrderListValue(generalOrderDoc2, LIST_VALUE_ID_2));
        caseData2.setGeneralOrders(generalOrders);

        final CaseDetails<CaseData, State> beforedetails = new CaseDetails<>();
        beforedetails.setData(caseData2);
        beforedetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> submitResponse = caseworkerRemoveGO.aboutToSubmit(afterdetails, beforedetails);

        verify(documentRemovalService).deleteDocument(anyList());
        assertNull(submitResponse.getData().getGeneralOrders());
    }
}
