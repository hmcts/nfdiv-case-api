package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.divorcecase.util.AccessCodeGenerator;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointJudicialSeparationContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingSolicitorContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingsWithAddressContent;
import uk.gov.hmcts.divorce.document.content.templatecontent.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.content.templatecontent.CoversheetSolicitorTemplateContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant1NoticeOfProceedingTest.caseData;
import static uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant1NoticeOfProceedingTest.caseDetails;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.DIGITAL_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1APP2_SOL_JS_JOINT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP2_JS_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT_JS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JS_SUBMITTED_RESPONDENT_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R1_SOLE_APP2_CIT_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_REISSUE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R2_SOLE_APP2_OUTSIDE_ENGLAND_WALES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_RS2_SOLE_APP2_SOL_OFFLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class GenerateApplicant2NoticeOfProceedingsTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Mock
    private NoticeOfProceedingContent noticeOfProceedingContent;

    @Mock
    private NoticeOfProceedingsWithAddressContent noticeOfProceedingsWithAddressContent;

    @Mock
    private NoticeOfProceedingJointContent noticeOfProceedingJointContent;

    @Mock
    private NoticeOfProceedingSolicitorContent noticeOfProceedingSolicitorContent;

    @Mock
    private NoticeOfProceedingJointJudicialSeparationContent noticeOfProceedingJointJudicialSeparationContent;

    @Mock
    private CoversheetSolicitorTemplateContent coversheetSolicitorTemplateContent;

    @Mock
    private GenerateCoversheet generateCoversheet;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    @Test
    void shouldGenerateRS2AndCoversheetWhenSoleWithAppRepresentedAndOffline() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(SOLE_APPLICATION, NO, YES);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), ENGLISH)).thenReturn(templateContent);
        when(coversheetSolicitorTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_RS2_SOLE_APP2_SOL_OFFLINE);
        verify(generateCoversheet)
            .generateCoversheet(
                caseData,
                TEST_CASE_ID,
                COVERSHEET_APPLICANT2_SOLICITOR,
                templateContent,
                ENGLISH
            );

        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNotNull();
        classMock.close();
    }

    @Test
    void shouldGenerateRS1WhenSoleWithAppRepresentedAndOnline() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(SOLE_APPLICATION, NO, YES);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .organisationPolicy(
                    OrganisationPolicy.<UserRole>builder()
                        .organisation(Organisation.builder().organisationId("orgID").build())
                        .build()
                )
                .build()
        );

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingsWithAddressContent.apply(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            ENGLISH)).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE);

        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNotNull();
        classMock.close();
    }

    @Test
    void shouldGenerateCoverLetterWhenSoleWithAppRepresentedAndOnlineAndSolicitorService() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(SOLE_APPLICATION, NO, YES);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .organisationPolicy(
                    OrganisationPolicy.<UserRole>builder()
                        .organisation(Organisation.builder().organisationId("orgID").build())
                        .build()
                )
                .build()
        );

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), ENGLISH)).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE);
        verify(generateCoversheet)
            .generateCoversheet(
                caseData,
                TEST_CASE_ID,
                COVERSHEET_APPLICANT2_SOLICITOR,
                templateContent,
                ENGLISH
            );
        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNotNull();
        classMock.close();
    }

    @Test
    void shouldGenerateR2WhenSoleWithAppNotRepresentedAndOffline() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(SOLE_APPLICATION, NO, NO);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setEmail(null);

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), ENGLISH)).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE);
        verify(generateCoversheet)
            .generateCoversheet(
                caseData,
                TEST_CASE_ID,
                COVERSHEET_APPLICANT,
                templateContent,
                ENGLISH
            );
        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNotNull();
        classMock.close();
    }

    @Test
    void shouldGenerateR2WhenSoleAndRespondentIsOverseas() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(SOLE_APPLICATION, NO, NO);
        caseData.getApplicant2().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("line 1")
                .postTown("town")
                .postCode("postcode")
                .country("France")
                .build()
        );
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), ENGLISH)).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_R2_SOLE_APP2_OUTSIDE_ENGLAND_WALES);
        verify(generateCoversheet)
            .generateCoversheet(
                caseData,
                TEST_CASE_ID,
                COVERSHEET_APPLICANT,
                templateContent,
                ENGLISH
            );
        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNotNull();
        classMock.close();
    }

    @Test
    void shouldGenerateR2WhenSoleWithAppNotRepresentedAndReissuedAsOfflineAos() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(SOLE_APPLICATION, NO, NO);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        LocalDate issueDate = LocalDate.now().minusDays(5);
        caseData.getApplication().setIssueDate(issueDate);
        caseData.getApplication().setReissueDate(issueDate.plusDays(5));
        caseData.getApplication().setReissueOption(ReissueOption.OFFLINE_AOS);

        caseData.getApplicant2().setEmail("respondent@email.com");

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), ENGLISH)).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_REISSUE);
        verify(generateCoversheet)
            .generateCoversheet(
                caseData,
                TEST_CASE_ID,
                COVERSHEET_APPLICANT,
                templateContent,
                ENGLISH
            );
        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNotNull();
        classMock.close();
    }

    @Test
    void shouldGenerateR1WhenSoleWithAppNotRepresentedAndOfflineButHasEmail() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(SOLE_APPLICATION, NO, NO);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setEmail("notnull@something.com");

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), ENGLISH)).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_R1_SOLE_APP2_CIT_ONLINE);

        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNotNull();
        classMock.close();
    }

    @Test
    void shouldGenerateR1WhenSoleWithAppNotRepresentedAndOnline() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(SOLE_APPLICATION, NO, NO);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setEmail("notnull@something.com");

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant1(), ENGLISH)).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_R1_SOLE_APP2_CIT_ONLINE);

        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNotNull();
        classMock.close();
    }

    @Test
    void shouldGenerateJSWhenSole() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(SOLE_APPLICATION, NO, NO);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setEmail("notnull@something.com");
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2(), ENGLISH)).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_APP2_JS_SOLE);

        verify(generateCoversheet)
            .generateCoversheet(
                caseData,
                TEST_CASE_ID,
                COVERSHEET_APPLICANT,
                templateContent,
                ENGLISH
            );
        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNotNull();
        classMock.close();
    }

    @Test
    void shouldGenerateJSWhenSoleRepresented() {
        setMockClock(clock);
        final CaseData caseData = caseData(SOLE_APPLICATION, NO, YES);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setEmail("notnull@something.com");
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplication().setReissueOption(ReissueOption.OFFLINE_AOS);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        final Map<String, Object> templateContent = new HashMap<>();

        verifyInteractions(caseData, templateContent, NFD_NOP_JS_SUBMITTED_RESPONDENT_SOLICITOR_TEMPLATE_ID);

        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNotNull();
    }

    @Test
    void shouldNotGenerateJSWhenSoleAndDigitalReissue() {
        setMockClock(clock);
        final CaseData caseData = caseData(SOLE_APPLICATION, NO, NO);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setEmail("notnull@something.com");
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.getApplication().setReissueOption(DIGITAL_AOS);

        final Map<String, Object> templateContent = new HashMap<>();

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyNoInteractions(coversheetSolicitorTemplateContent, noticeOfProceedingSolicitorContent);

        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNotNull();
    }

    @Test
    void shouldGenerateAS1WhenJointWithAppRepresented() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(JOINT_APPLICATION, NO, YES);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setEmail("notnull@something.com");

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingSolicitorContent.apply(caseData, TEST_CASE_ID, false)).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS);

        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNull();
        classMock.close();
    }

    @Test
    void shouldGenerateJA1WhenJointWithAppNotRepresented() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(JOINT_APPLICATION, NO, NO);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setEmail("notnull@something.com");

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingJointContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_JA1_JOINT_APP1APP2_CIT);

        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNull();
        classMock.close();
    }

    @Test
    void shouldGenerateJointCitizenJudicialSeparationNoticeOfProceedingsAndCoversheet() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(JOINT_APPLICATION, NO, NO);
        caseData.getApplicant2().setEmail("notnull@something.com");
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingJointJudicialSeparationContent.apply(caseData, TEST_CASE_ID,
            caseData.getApplicant2(), caseData.getApplicant1())).thenReturn(templateContent);
        when(coversheetApplicantTemplateContent.apply(caseData, TEST_CASE_ID, caseData.getApplicant2())).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_JA1_JOINT_APP1APP2_CIT_JS);

        verify(generateCoversheet)
            .generateCoversheet(
                caseData,
                TEST_CASE_ID,
                COVERSHEET_APPLICANT,
                templateContent,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, COVERSHEET_DOCUMENT_NAME, "applicant2", now(clock))
            );

        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNull();
        classMock.close();
    }

    @Test
    void shouldGenerateJointCitizenJudicialSeparationNoticeOfProceedingsAndCoversheetForApplicant2Solicitor() {
        setMockClock(clock);
        MockedStatic<AccessCodeGenerator> classMock = mockStatic(AccessCodeGenerator.class);
        classMock.when(AccessCodeGenerator::generateAccessCode).thenReturn(ACCESS_CODE);

        final CaseData caseData = caseData(JOINT_APPLICATION, YES, YES);
        caseData.getApplicant2().setEmail("notnull@something.com");
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.getApplicant1().setSolicitorRepresented(YES);

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingSolicitorContent.apply(caseData, TEST_CASE_ID, false)).thenReturn(templateContent);
        when(coversheetSolicitorTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateApplicant2NoticeOfProceedings.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NFD_NOP_APP1APP2_SOL_JS_JOINT);

        verify(generateCoversheet)
            .generateCoversheet(
                caseData,
                TEST_CASE_ID,
                    COVERSHEET_APPLICANT2_SOLICITOR,
                templateContent,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, COVERSHEET_DOCUMENT_NAME, "applicant2", now(clock))
            );

        assertThat(result.getData()).isEqualTo(caseData);
        assertThat(result.getData().getCaseInvite().accessCode()).isNull();
        classMock.close();
    }

    private void verifyInteractions(CaseData caseData, Map<String, Object> templateContent,
                                    String templateId) {
        verify(caseDataDocumentService, times(1))
            .renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS_APP_2,
                templateContent,
                TEST_CASE_ID,
                templateId,
                caseData.getApplicant2().getLanguagePreference(),
                formatDocumentName(TEST_CASE_ID, NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME, now(clock))
            );
    }
}
