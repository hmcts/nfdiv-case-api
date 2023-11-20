package uk.gov.hmcts.divorce.systemupdate.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateApplyForConditionalOrderDocument.FIRST_NAME;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateApplyForConditionalOrderDocument.LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicantWithAddress;

@ExtendWith(MockitoExtension.class)
public class GenerateApplyForConditionalOrderDocumentTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateApplyForConditionalOrderDocument generateApplyForConditionalOrderDocument;

    @Test
    void shouldGenerateWithSoleDivorceContent() {
        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(FIRST_NAME, TEST_FIRST_NAME);
        templateContent.put(LAST_NAME, TEST_LAST_NAME);
        templateContent.put(ADDRESS, "line 1\ntown\npostcode");
        templateContent.put(PARTNER, "husband");
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, false);
        templateContent.put(IS_DIVORCE, true);
        templateContent.putAll(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()));

        generateApplyForConditionalOrderDocument.generateApplyForConditionalOrder(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2()
        );

        verify(commonContent)
            .templateContentCanApplyForCoOrFo(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant1(),
                caseData.getApplicant2(),
                getExpectedLocalDate()
            );
    }

    @Test
    void shouldGenerateWithJointDivorceContent() {
        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(FIRST_NAME, TEST_FIRST_NAME);
        templateContent.put(LAST_NAME, TEST_LAST_NAME);
        templateContent.put(ADDRESS, "line 1\ntown\npostcode");
        templateContent.put(PARTNER, "husband");
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, true);
        templateContent.put(IS_DIVORCE, true);
        templateContent.putAll(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()));

        generateApplyForConditionalOrderDocument.generateApplyForConditionalOrder(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2()
        );

        verify(commonContent)
            .templateContentCanApplyForCoOrFo(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant1(),
                caseData.getApplicant2(),
                getExpectedLocalDate()
            );
    }

    @Test
    void shouldGenerateWithDissolutionContent() {
        setMockClock(clock);

        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        final Map<String, Object> templateContent = new HashMap<>();
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));
        templateContent.put(FIRST_NAME, TEST_FIRST_NAME);
        templateContent.put(LAST_NAME, TEST_LAST_NAME);
        templateContent.put(ADDRESS, "line 1\ntown\npostcode");
        templateContent.put(PARTNER, "civil partner");
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, false);
        templateContent.put(IS_DIVORCE, false);
        templateContent.putAll(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()));

        generateApplyForConditionalOrderDocument.generateApplyForConditionalOrder(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2()
        );

        verify(commonContent)
            .templateContentCanApplyForCoOrFo(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant1(),
                caseData.getApplicant2(),
                getExpectedLocalDate()
            );
    }
}
