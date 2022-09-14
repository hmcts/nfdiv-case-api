package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class GenerateFinalOrderCoverLetterTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateFinalOrderCoverLetter generateFinalOrderCoverLetter;

    @Test
    void shouldGenerateCoverLetter() {

        setMockClock(clock);

        CaseData caseData = caseData();

        Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(NAME, join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName()));
        templateContent.put(ADDRESS, caseData.getApplicant1().getPostalAddress());
        templateContent.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(IS_DIVORCE, caseData.getDivorceOrDissolution().isDivorce());

        generateFinalOrderCoverLetter.apply(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            FINAL_ORDER_GRANTED_COVER_LETTER_APP_1
        );

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            FINAL_ORDER_GRANTED_COVER_LETTER_APP_1,
            templateContent,
            TEST_CASE_ID,
            FINAL_ORDER_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(FINAL_ORDER_COVER_LETTER_DOCUMENT_NAME, now(clock))
        );
    }
}
