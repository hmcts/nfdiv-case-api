package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ApplicationSoleTemplateContent;
import uk.gov.hmcts.divorce.document.content.DivorceApplicationJointTemplateContent;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_JOINT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JUDICIAL_SEPARATION_SOLE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
class GenerateApplicationTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private ApplicationSoleTemplateContent applicationSoleTemplateContent;

    @Mock
    private DivorceApplicationJointTemplateContent divorceApplicationJointTemplateContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateApplication generateApplication;

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithSoleDivorceApplicationDocumentForSoleApplication() {

        setMockClock(clock);

        final var caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .application(Application.builder()
                .solSignStatementOfTruth(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Map<String, Object> templateContent = new HashMap<>();

        when(applicationSoleTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateApplication.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                APPLICATION,
                templateContent,
                TEST_CASE_ID,
                DIVORCE_APPLICATION_SOLE,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, DIVORCE_APPLICATION_DOCUMENT_NAME, now(clock))
            );

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithJointApplicationDocumentForJointApplication() {

        setMockClock(clock);

        final var caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(YES)
                .build())
            .application(Application.builder()
                .solSignStatementOfTruth(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Map<String, Object> templateContent = new HashMap<>();

        when(divorceApplicationJointTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateApplication.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                APPLICATION,
                templateContent,
                TEST_CASE_ID,
                DIVORCE_APPLICATION_JOINT,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, DIVORCE_APPLICATION_DOCUMENT_NAME, now(clock))
            );

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithJointApplicationDocumentInWelshForJointApplication() {

        setMockClock(clock);

        final var caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(YES)
                .build())
            .applicant2(Applicant.builder()
                .languagePreferenceWelsh(YES)
                .build())
            .application(Application.builder()
                .solSignStatementOfTruth(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Map<String, Object> templateContent = new HashMap<>();

        when(divorceApplicationJointTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateApplication.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                APPLICATION,
                templateContent,
                TEST_CASE_ID,
                DIVORCE_APPLICATION_JOINT,
                WELSH,
                formatDocumentName(TEST_CASE_ID, DIVORCE_APPLICATION_DOCUMENT_NAME, now(clock))
            );

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithSoleJudicialSeparationApplicationDocumentForSoleApplication() {

        setMockClock(clock);

        final var caseData = CaseData.builder()
                .applicationType(SOLE_APPLICATION)
                .applicant1(Applicant.builder()
                        .languagePreferenceWelsh(NO)
                        .build())
                .application(Application.builder()
                        .solSignStatementOfTruth(YES)
                        .build())
                .isJudicialSeparation(YES)
                .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Map<String, Object> templateContent = new HashMap<>();

        when(applicationSoleTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateApplication.apply(caseDetails);

        verify(caseDataDocumentService)
                .renderDocumentAndUpdateCaseData(
                    caseData,
                    APPLICATION,
                    templateContent,
                    TEST_CASE_ID,
                    NFD_NOP_JUDICIAL_SEPARATION_SOLE_TEMPLATE_ID,
                    ENGLISH,
                    formatDocumentName(TEST_CASE_ID, JUDICIAL_SEPARATION_APPLICATION_DOCUMENT_NAME, now(clock))
            );

        assertThat(result.getData()).isEqualTo(caseData);
    }
}
