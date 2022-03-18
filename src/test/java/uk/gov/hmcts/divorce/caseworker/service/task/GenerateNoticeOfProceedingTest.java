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
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointContent;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.*;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
class GenerateNoticeOfProceedingTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private NoticeOfProceedingContent noticeOfProceedingContent;

    @Mock
    private NoticeOfProceedingJointContent noticeOfProceedingJointContent;

    @InjectMocks
    private GenerateNoticeOfProceeding generateNoticeOfProceeding;

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithSoleDivorceApplicationDocumentForSoleApplicationWhenRespondentIsNotOverseas() {

        final CaseData caseData = caseData(SOLE_APPLICATION, NO, NO);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().addressLine1("line1").country("UK").build());

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NOTICE_OF_PROCEEDINGS_TEMPLATE_ID, 1);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithSoleDivorceApplicationDocumentForSoleApplicationWhenRespondentIsRepresented() {

        final CaseData caseData = caseData(SOLE_APPLICATION, NO, YES);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().addressLine1("line1").country("UK").build());

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NOTICE_OF_PROCEEDINGS_RESP_TEMPLATE_ID, 1);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithSoleDivorceApplicationDocumentForSoleApplicationWhenRespondentIsOverseas() {

        final CaseData caseData = caseData(SOLE_APPLICATION, NO, NO);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().addressLine1("line1").country("France").build());

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NOTICE_OF_PROCEEDINGS_OVERSEAS_RESP_TEMPLATE_ID, 1);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithJointDivorceApp1RepresentedApp2Not() {

        final CaseData caseData = caseData(JOINT_APPLICATION, YES, NO);

        final Map<String, Object> templateContentApplicant2 = new HashMap<>();

        when(noticeOfProceedingJointContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2())).thenReturn(templateContentApplicant2);

        final var result = generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContentApplicant2, JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID, 1);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithJointDivorceApp1NotRepresentedApp2Not() {

        final CaseData caseData = caseData(JOINT_APPLICATION, NO, NO);

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingJointContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1())).thenReturn(templateContent);
        when(noticeOfProceedingJointContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2())).thenReturn(templateContent);

        final var result = generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID, 2);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithJointDivorceApp1RepresentedApp2Represented() {

        final CaseData caseData = caseData(JOINT_APPLICATION, YES, YES);

        final var result = generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyNoInteractions(caseDataDocumentService);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithJointDivorceApp1OfflineApp2Online() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder()
                .offline(YES)
                .solicitorRepresented(YES)
                .languagePreferenceWelsh(NO)
                .build())
            .applicant2(Applicant.builder()
                .email("onlineApplicant2@email.com")
                .solicitorRepresented(YES)
                .build())
            .application(Application.builder()
                .solSignStatementOfTruth(NO)
                .build())
            .build();

        final Map<String, Object> templateContentApplicant2 = new HashMap<>();

        when(noticeOfProceedingJointContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1())).thenReturn(templateContentApplicant2);

        final var result = generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContentApplicant2, JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID, 1);

        assertThat(result.getData()).isEqualTo(caseData);
    }


    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithJointDivorceApp1OnlineApp2Offline() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder()
                .offline(NO)
                .solicitorRepresented(YES)
                .languagePreferenceWelsh(NO)
                .build())
            .applicant2(Applicant.builder()
                .offline(YES)
                .solicitorRepresented(YES)
                .build())
            .application(Application.builder()
                .solSignStatementOfTruth(NO)
                .build())
            .build();

        final Map<String, Object> templateContentApplicant2 = new HashMap<>();

        when(noticeOfProceedingJointContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2())).thenReturn(templateContentApplicant2);

        final var result = generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContentApplicant2, JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID, 1);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithJointDivorceApp1OnlineApp2Online() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder()
                .offline(NO)
                .solicitorRepresented(YES)
                .languagePreferenceWelsh(NO)
                .build())
            .applicant2(Applicant.builder()
                .solicitorRepresented(YES)
                .email("onlineApplicant2@email.com")
                .build())
            .application(Application.builder()
                .solSignStatementOfTruth(NO)
                .build())
            .build();

        final var result = generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyNoInteractions(caseDataDocumentService);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithJointDivorceApp1OfflineApp2Offline() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .applicant1(Applicant.builder()
                .offline(YES)
                .solicitorRepresented(YES)
                .languagePreferenceWelsh(NO)
                .build())
            .applicant2(Applicant.builder()
                .solicitorRepresented(YES)
                .build())
            .application(Application.builder()
                .solSignStatementOfTruth(NO)
                .build())
            .build();

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingJointContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1())).thenReturn(templateContent);
        when(noticeOfProceedingJointContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2())).thenReturn(templateContent);

        final var result = generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, JOINT_NOTICE_OF_PROCEEDINGS_TEMPLATE_ID, 2);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    private void verifyInteractions(CaseData caseData, Map<String, Object> templateContent, String templateId, int times) {
        verify(caseDataDocumentService, times(times))
            .renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS,
                templateContent,
                TEST_CASE_ID,
                templateId,
                caseData.getApplicant1().getLanguagePreference(),
                NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME
            );
    }

    private CaseDetails<CaseData, State> caseDetails(CaseData caseData) {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        return caseDetails;
    }

    private CaseData caseData(ApplicationType applicationType, YesOrNo isApp1Represented, YesOrNo isApp2Represented) {
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
            .build();
    }
}
