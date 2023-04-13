package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_JS_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.SWITCH_TO_SOLE_CO_LETTER;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APP2_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;

@ExtendWith(MockitoExtension.class)
class GenerateSwitchToSoleConditionalOrderJSLetterTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateSwitchToSoleConditionalOrderJSLetter underTest;

    @Test
    void shouldRenderDocumentAndUpdateCaseData() {
        CaseData caseData = caseData();
        Applicant respondent = Applicant.builder()
            .firstName(TEST_APP2_FIRST_NAME)
            .lastName(TEST_APP2_LAST_NAME)
            .offline(YES)
            .address(AddressGlobalUK.builder().addressLine1("line1").addressLine2("line2").postCode("postcode").build())
            .languagePreferenceWelsh(NO)
            .build();
        caseData.setApplicant2(respondent);
        caseData.setApplicationType(SOLE_APPLICATION);

        setMockClock(clock);

        Map<String, Object> expectedTemplateContent = new HashMap<>(getBasicDocmosisTemplateContent(respondent.getLanguagePreference()));

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(
            respondent.getLanguagePreference())).thenReturn(expectedTemplateContent);

        when(commonContent.getPartner(caseData, caseData.getApplicant1(), respondent.getLanguagePreference()))
            .thenReturn("husband");

        underTest.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), respondent);

        expectedTemplateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        expectedTemplateContent.put(IS_DIVORCE, true);
        expectedTemplateContent.put(FIRST_NAME, TEST_APP2_FIRST_NAME);
        expectedTemplateContent.put(LAST_NAME, TEST_APP2_LAST_NAME);
        expectedTemplateContent.put(ADDRESS, "line1\nline2\npostcode");
        expectedTemplateContent.put(PARTNER, "husband");
        expectedTemplateContent.put(DATE, now(clock).format(DATE_TIME_FORMATTER));

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            SWITCH_TO_SOLE_CO_LETTER,
            expectedTemplateContent,
            TEST_CASE_ID,
            SWITCH_TO_SOLE_CO_JS_LETTER_TEMPLATE_ID,
            ENGLISH,
            formatDocumentName(TEST_CASE_ID, SWITCH_TO_SOLE_CO_LETTER_DOCUMENT_NAME, now(clock))
        );
    }
}