package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingSolicitorContent;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_A1_SOLE_APP1_CIT_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_A2_SOLE_APP1_CIT_PS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS2_SOLE_APP1_SOL_SS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
class GenerateApplicant1NoticeOfProceedingTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private NoticeOfProceedingContent noticeOfProceedingContent;

    @Mock
    private NoticeOfProceedingJointContent noticeOfProceedingJointContent;

    @Mock
    private NoticeOfProceedingSolicitorContent noticeOfProceedingSolicitorContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateApplicant1NoticeOfProceeding generateApplicant1NoticeOfProceeding;

    @Test
    void shouldGenerateA1WhenSoleWithAppNotRepresentedAndCourtService() {

        setMockClock(clock);

        final CaseData caseData = caseData(SOLE_APPLICATION, NO);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().addressLine1("line1").country("UK").build());

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2())).thenReturn(templateContent);

        final var result = generateApplicant1NoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_A1_SOLE_APP1_CIT_CS);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldGenerateA2WhenSoleWithAppNotRepresentedAndPersonalService() {

        setMockClock(clock);

        final CaseData caseData = caseData(SOLE_APPLICATION, NO);
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().addressLine1("line1").country("France").build());

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2())).thenReturn(templateContent);

        final var result = generateApplicant1NoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_A2_SOLE_APP1_CIT_PS);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldGenerateAS1WhenSoleWithAppRepresentedAndCourtService() {

        setMockClock(clock);

        final CaseData caseData = caseData(SOLE_APPLICATION, YES);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingSolicitorContent.apply(caseData, TEST_CASE_ID, true)).thenReturn(templateContent);

        final var result = generateApplicant1NoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldGenerateAS2WhenSoleWithAppRepresentedAndSolicitorService() {

        setMockClock(clock);

        final CaseData caseData = caseData(SOLE_APPLICATION, YES);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingSolicitorContent.apply(caseData, TEST_CASE_ID, true)).thenReturn(templateContent);

        final var result = generateApplicant1NoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_AS2_SOLE_APP1_SOL_SS);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldGenerateAS1WhenJointWithAppRepresented() {

        setMockClock(clock);

        final CaseData caseData = caseData(JOINT_APPLICATION, YES);

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingSolicitorContent.apply(caseData, TEST_CASE_ID, true)).thenReturn(templateContent);

        final var result = generateApplicant1NoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldGenerateJA1WhenJointWithAppNotRepresented() {

        setMockClock(clock);

        final CaseData caseData = caseData(JOINT_APPLICATION, NO);

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingJointContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(templateContent);

        final var result = generateApplicant1NoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_JA1_JOINT_APP1APP2_CIT);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    private void verifyInteractions(CaseData caseData, Map<String, Object> templateContent,
                                    String templateId) {
        verify(caseDataDocumentService, times(1))
            .renderDocumentAndUpdateCaseData(
                caseData,
                DocumentType.NOTICE_OF_PROCEEDINGS_APP_1,
                templateContent,
                TEST_CASE_ID,
                templateId,
                caseData.getApplicant1().getLanguagePreference(),
                formatDocumentName(TEST_CASE_ID, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME, now(clock))
            );
    }

    public static CaseDetails<CaseData, State> caseDetails(CaseData caseData) {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        return caseDetails;
    }

    public static CaseData caseData(ApplicationType applicationType, YesOrNo isApp1Represented) {
        return caseData(applicationType, isApp1Represented, NO);
    }

    public static CaseData caseData(ApplicationType applicationType, YesOrNo isApp1Represented, YesOrNo isApp2Represented) {
        return CaseData.builder()
            .applicationType(applicationType)
            .applicant1(Applicant.builder()
                .solicitorRepresented(isApp1Represented)
                .languagePreferenceWelsh(NO)
                .build())
            .applicant2(Applicant.builder()
                .solicitorRepresented(isApp2Represented)
                .email("onlineApplicant2@email.com")
                .build())
            .application(Application.builder()
                .solSignStatementOfTruth(NO)
                .build())
            .caseInvite(CaseInvite.builder().build())
            .build();
    }
}
