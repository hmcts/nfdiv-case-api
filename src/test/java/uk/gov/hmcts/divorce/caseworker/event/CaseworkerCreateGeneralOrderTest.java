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
import uk.gov.hmcts.divorce.caseworker.event.page.CreateGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerCreateGeneralOrder.CASEWORKER_CREATE_GENERAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceGeneralOrderListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getGeneralOrder;

@ExtendWith(MockitoExtension.class)
public class CaseworkerCreateGeneralOrderTest {
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
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = ConfigTestUtil.createCaseDataConfigBuilder();

        caseworkerCreateGeneralOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .containsExactly(CASEWORKER_CREATE_GENERAL_ORDER);
    }

    @Test
    public void shouldSetGeneralOrderDocumentsWhenThereIsNoExistingGeneralOrder() throws Exception {
        final CaseData caseData = caseData();

        String documentUrl = "http://localhost:8080/1234";

        Document generalOrder = new Document(
            documentUrl,
            "generalOrder2021-07-16 11:10:34.pdf",
            documentUrl + "/binary"
        );

        caseData.setGeneralOrder(getGeneralOrder(generalOrder));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);

        when(documentIdProvider.documentId()).thenReturn(LIST_VALUE_ID_1);

        AboutToStartOrSubmitResponse<CaseData, State> submitResponse = caseworkerCreateGeneralOrder.aboutToSubmit(details, details);

        assertThat(submitResponse.getData().getGeneralOrder()).isNull();
        assertThat(submitResponse.getData().getGeneralOrders())
            .containsExactly(getDivorceGeneralOrderListValue(generalOrder, LIST_VALUE_ID_1));
    }

    @Test
    public void shouldAddLatestGeneralOrderDocumentToTopWhenThereIsExistingGeneralOrder() throws Exception {
        final CaseData caseData = caseData();

        String documentUrl = "http://localhost:8080/4567";

        Document generalOrder1 = new Document(
            documentUrl,
            "generalOrder2020-07-16 11:10:34.pdf",
            documentUrl + "/binary"
        );

        Document generalOrder2 = new Document(
            documentUrl,
            "generalOrder2021-07-16 11:10:34.pdf",
            documentUrl + "/binary"
        );

        caseData.setGeneralOrder(getGeneralOrder(generalOrder2));

        final List<ListValue<DivorceGeneralOrder>> generalOrders = new ArrayList<>();
        generalOrders.add(getDivorceGeneralOrderListValue(generalOrder1, LIST_VALUE_ID_1));
        caseData.setGeneralOrders(generalOrders);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);

        when(documentIdProvider.documentId()).thenReturn(LIST_VALUE_ID_2);

        AboutToStartOrSubmitResponse<CaseData, State> submitResponse = caseworkerCreateGeneralOrder.aboutToSubmit(details, details);

        assertThat(submitResponse.getData().getGeneralOrder()).isNull();
        assertThat(submitResponse.getData().getGeneralOrders()).containsExactly(
            getDivorceGeneralOrderListValue(generalOrder2, LIST_VALUE_ID_2),
            getDivorceGeneralOrderListValue(generalOrder1, LIST_VALUE_ID_1)
        );
    }
}
