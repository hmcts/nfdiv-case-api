package uk.gov.hmcts.divorce.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.ScannedGeneralOrderOrGeneratedGeneralOrder.GENERATED_GENERAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.ScannedGeneralOrderOrGeneratedGeneralOrder.SCANNED_GENERAL_ORDER;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_GENERAL_ORDER;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addScannedDocument;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getGeneralOrder;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getGeneralOrderDocument;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getScannedGeneralOrderDocument;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.setScannedDocumentNames;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.setSelectedScannedDocument;

@ExtendWith(MockitoExtension.class)
class CreateGeneralOrderTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private GeneralOrderTemplateContent generalOrderTemplateContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private CreateGeneralOrder createGeneralOrder;

    @Test
    void shouldUpdateCaseWithGeneralOrderDocumentWhenMidEventCallbackIsTriggered() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);
        caseData.setGeneralOrder(getGeneralOrder());

        final Map<String, Object> templateContent = new HashMap<>();

        when(generalOrderTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final LocalDateTime dateTime = LocalDateTime.of(2021, Month.JUNE, 15, 13, 39);
        final Instant instant = dateTime.atZone(ZoneId.of("Europe/London")).toInstant();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZoneId.of("Europe/London"));

        Document generalOrderDocument = getGeneralOrderDocument();

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                DIVORCE_GENERAL_ORDER,
                ENGLISH,
                GENERAL_ORDER + "2021-06-15 13:39:00"
            ))
            .thenReturn(generalOrderDocument);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> midEventResponse =
            createGeneralOrder.midEvent(details, details);

        GeneralOrder actualGeneralOrder = midEventResponse.getData().getGeneralOrder();

        assertThat(actualGeneralOrder.getGeneralOrderDraft()).isEqualTo(generalOrderDocument);
        assertThat(actualGeneralOrder.getScannedGeneralOrderOrGeneratedGeneralOrder()).isEqualTo(GENERATED_GENERAL_ORDER);

        verify(generalOrderTemplateContent).apply(caseData, TEST_CASE_ID);
        verify(caseDataDocumentService).renderDocument(
            templateContent,
            TEST_CASE_ID,
            DIVORCE_GENERAL_ORDER,
            ENGLISH,
            GENERAL_ORDER + "2021-06-15 13:39:00"
        );
    }

    @Test
    void shouldUpdateCaseWithScannedGeneralOrderDocumentWhenMidEventCallbackIsTriggered() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        final ScannedDocument scannedGeneralOrderDocument = getScannedGeneralOrderDocument();

        addScannedDocument(caseData, scannedGeneralOrderDocument);
        setScannedDocumentNames(caseData);
        setSelectedScannedDocument(caseData, 0);
        caseData.setGeneralOrder(getGeneralOrder(scannedGeneralOrderDocument));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> midEventResponse =
            createGeneralOrder.midEvent(details, details);

        GeneralOrder actualGeneralOrder = midEventResponse.getData().getGeneralOrder();


        verifyNoInteractions(generalOrderTemplateContent);
        verifyNoInteractions(caseDataDocumentService);

        assertThat(actualGeneralOrder.getGeneralOrderScannedDraft()).isEqualTo(scannedGeneralOrderDocument);
        assertThat(actualGeneralOrder.getScannedGeneralOrderOrGeneratedGeneralOrder()).isEqualTo(SCANNED_GENERAL_ORDER);
    }
}
