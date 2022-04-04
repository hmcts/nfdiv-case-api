package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.util.AccessCodeGenerator;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CitizenRespondentAosInvitationTemplateContent;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicant2TemplateContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R1_SOLE_APP2_CIT_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;

@ExtendWith(MockitoExtension.class)
public class GenerateApplicant2NoticeOfProceedingsTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private NoticeOfProceedingContent noticeOfProceedingContent;

    @Mock
    private CitizenRespondentAosInvitationTemplateContent citizenRespondentAosInvitationTemplateContent;

    @Mock
    private CoversheetApplicant2TemplateContent coversheetApplicant2TemplateContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    @Test
    void shouldReturnCaseDataWithAosInvitationDocumentIfRespondentIsRepresentedAndIsSoleApplication() {
        setMockClock(clock);

        final var caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.setApplicant2(respondentWithDigitalSolicitor());
        caseData.setApplicationType(SOLE_APPLICATION);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final Map<String, Object> templateContent = new HashMap<>();

        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails);

        assertThat(result.getData().getCaseInvite().accessCode()).isEqualTo(ACCESS_CODE);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS_APP_2,
                templateContent,
                TEST_CASE_ID,
                NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME, LocalDateTime.now(clock))
            );

        classMock.close();
    }

    @Test
    void shouldGenerateAosInvitationDocOnlineVersionIfRespondentIsNotRepresentedAndHasEmailAndIsSole() {
        setMockClock(clock);

        final var caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setEmail("respondentemail@test.com");

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(citizenRespondentAosInvitationTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        generateApplicant2NoticeOfProceedings.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS_APP_2,
                templateContent,
                TEST_CASE_ID,
                NFD_NOP_R1_SOLE_APP2_CIT_ONLINE,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME, LocalDateTime.now(clock))
            );

        verifyNoMoreInteractions(caseDataDocumentService);
    }

    @Test
    void shouldGenerateAosInvitationDocOfflineVersionAndCoversheetIfRespondentIsNotRepresentedAndDoesNotHaveEmailAndIsSole() {
        setMockClock(clock);

        final var caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.setApplicationType(SOLE_APPLICATION);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        final Map<String, Object> coversheetContent = new HashMap<>();

        when(citizenRespondentAosInvitationTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);
        when(coversheetApplicant2TemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(coversheetContent);

        generateApplicant2NoticeOfProceedings.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS_APP_2,
                templateContent,
                TEST_CASE_ID,
                NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME, LocalDateTime.now(clock))
            );

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                COVERSHEET,
                templateContent,
                TEST_CASE_ID,
                COVERSHEET_APPLICANT,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, COVERSHEET_DOCUMENT_NAME, LocalDateTime.now(clock))
            );

        verifyNoMoreInteractions(caseDataDocumentService);
    }

    @Test
    void shouldGenerateAosOfflineVersionAndCoversheetIfRespIsNotRepresentedAndDoesNotHaveEmailAndApp1KnowsApp2Address() {
        setMockClock(clock);

        final var caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setApplicant1KnowsApplicant2Address(YES);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        final Map<String, Object> coversheetContent = new HashMap<>();
        final MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        when(citizenRespondentAosInvitationTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);
        when(coversheetApplicant2TemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(coversheetContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails);

        assertThat(result.getData().getCaseInvite().accessCode()).isEqualTo(ACCESS_CODE);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS_APP_2,
                templateContent,
                TEST_CASE_ID,
                NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME, LocalDateTime.now(clock))
            );

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                COVERSHEET,
                templateContent,
                TEST_CASE_ID,
                COVERSHEET_APPLICANT,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, COVERSHEET_DOCUMENT_NAME, LocalDateTime.now(clock))
            );

        verifyNoMoreInteractions(caseDataDocumentService);

        classMock.close();
    }

    @Test
    void shouldNotGenerateAosInvitationDocOfflineVersionAndCoversheetIfRespondentIAddressIsNotKnownByApplicantAndIsSole() {
        final var caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setApplicant1KnowsApplicant2Address(NO);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails);

        assertThat(result.getData().getCaseInvite().accessCode()).isEqualTo(ACCESS_CODE);

        verifyNoInteractions(caseDataDocumentService);

        classMock.close();
    }

}
