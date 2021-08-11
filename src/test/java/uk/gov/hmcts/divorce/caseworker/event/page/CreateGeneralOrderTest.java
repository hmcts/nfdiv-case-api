package uk.gov.hmcts.divorce.caseworker.event.page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.GeneralOrderTemplateContent;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_GENERAL_ORDER;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getGeneralOrder;

@ExtendWith(MockitoExtension.class)
public class CreateGeneralOrderTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private GeneralOrderTemplateContent generalOrderTemplateContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private CreateGeneralOrder createGeneralOrder;

    @BeforeEach
    void setClock() {
        LocalDateTime dateTime = LocalDateTime.of(2021, Month.JUNE, 15, 13, 39);
        Instant instant = dateTime.atZone(ZoneId.of("Europe/London")).toInstant();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZoneId.of("Europe/London"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldUpdateCaseWithGeneralOrderDocumentWhenMidEventCallbackIsTriggered() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        caseData.setGeneralOrder(getGeneralOrder());

        final Supplier<Map<String, Object>> templateContentSupplier = HashMap::new;

        when(generalOrderTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContentSupplier);

        String documentUrl = "http://localhost:8080/4567";
        Document generalOrderDocument = new Document(
            documentUrl,
            "generalOrder2020-07-16 11:10:34.pdf",
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderGeneralOrderDocument(
                eq(templateContentSupplier),
                eq(TEST_CASE_ID),
                eq(DIVORCE_GENERAL_ORDER),
                eq(ENGLISH),
                any()
            ))
            .thenReturn(generalOrderDocument);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> midEventResponse =
            createGeneralOrder.midEvent(details, details);

        GeneralOrder actualGeneralOrder = midEventResponse.getData().getGeneralOrder();

        assertThat(actualGeneralOrder.getGeneralOrderDraft()).isEqualTo(generalOrderDocument);

        verify(generalOrderTemplateContent).apply(caseData, TEST_CASE_ID);
        verify(caseDataDocumentService).renderGeneralOrderDocument(
            templateContentSupplier,
            TEST_CASE_ID,
            DIVORCE_GENERAL_ORDER,
            ENGLISH,
            GENERAL_ORDER + "2021-06-15 13:39:00"
        );
    }
}
