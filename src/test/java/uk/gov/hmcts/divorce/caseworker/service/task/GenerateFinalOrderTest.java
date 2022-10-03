package uk.gov.hmcts.divorce.caseworker.service.task;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.FinalOrderGrantedTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataForGrantFinalOrder;

@ExtendWith(MockitoExtension.class)
public class GenerateFinalOrderTest {

    private static final LocalDate DATE = LocalDate.of(2022, 3, 16);
    private static final String FILE_NAME = "FinalOrderGranted-2022-03-16:00:00";

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private FinalOrderGrantedTemplateContent finalOrderGrantedTemplateContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateFinalOrder generateFinalOrder;

    @BeforeEach
    public void setUp() {
        setMockClock(clock, DATE);
    }

    @Test
    public void shouldGenerateFinalOrderAndUpdateCaseData() {

        CaseData caseData = buildCaseDataForGrantFinalOrder(ApplicationType.SOLE_APPLICATION, DivorceOrDissolution.DIVORCE);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(finalOrderGrantedTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateFinalOrder.apply(caseDetails);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            DocumentType.FINAL_ORDER_GRANTED,
            templateContent,
            TEST_CASE_ID,
            FINAL_ORDER_TEMPLATE_ID,
            ENGLISH,
            FILE_NAME
        );

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    public void shouldRemoveFinalOrderGrantedDoc() {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(
                    ListValue.<DivorceDocument>builder()
                        .id("1")
                        .value(DivorceDocument.builder()
                            .documentType(FINAL_ORDER_GRANTED)
                            .build())
                        .build(),
                    ListValue.<DivorceDocument>builder()
                        .id("2")
                        .value(DivorceDocument.builder()
                            .documentType(APPLICATION)
                            .build()).build()
                ))
                .build())
            .build();

        final Map<String, Object> templateContent = new HashMap<>();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        when(finalOrderGrantedTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        generateFinalOrder.removeExistingAndGenerateNewFinalOrderGrantedDoc(caseDetails);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            DocumentType.FINAL_ORDER_GRANTED,
            templateContent,
            TEST_CASE_ID,
            FINAL_ORDER_TEMPLATE_ID,
            ENGLISH,
            FILE_NAME
        );

        assertEquals(1, caseData.getDocuments().getDocumentsGenerated().size());
        assertEquals(APPLICATION, caseData.getDocuments().getDocumentsGenerated().get(0).getValue().getDocumentType());
    }
}
