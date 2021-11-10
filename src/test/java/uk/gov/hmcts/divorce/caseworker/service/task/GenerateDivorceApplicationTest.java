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
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class GenerateDivorceApplicationTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private NoticeOfProceedingContent noticeOfProceedingContent;

    @InjectMocks
    private GenerateNoticeOfProceeding generateNoticeOfProceeding;

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithNoticeOfProceedingDocForSoleApplication() {

        final var caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .application(Application.builder()
                .solSignStatementOfTruth(NO)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        generateNoticeOfProceeding.apply(caseDetails);

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                NOTICE_OF_PROCEEDINGS,
                noticeOfProceedingContent.apply(caseData, TEST_CASE_ID),
                TEST_CASE_ID,
                NOTICE_OF_PROCEEDINGS_TEMPLATE_ID,
                caseData.getApplicant1().getLanguagePreference(),
                NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME
            );
    }

    @Test
    void shouldNotCallDocAssemblyServiceForJointApplication() {

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

        generateNoticeOfProceeding.apply(caseDetails);

        verifyNoInteractions(caseDataDocumentService);
    }

    @Test
    void shouldNotCallDocAssemblyServiceForSolicitorApplication() {

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

        generateNoticeOfProceeding.apply(caseDetails);

        verifyNoInteractions(caseDataDocumentService);
    }
}
