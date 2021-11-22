package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_OVERSEAS_RESP_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
class GenerateNoticeOfProceedingTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private NoticeOfProceedingContent noticeOfProceedingContent;

    @InjectMocks
    private GenerateNoticeOfProceeding generateNoticeOfProceeding;

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithSoleDivorceApplicationDocumentForSoleApplicationWhenRespondentIsNotOverseas() {

        final CaseData caseData = caseData(SOLE_APPLICATION);
        caseData.getApplicant2().setHomeAddress(AddressGlobalUK.builder().addressLine1("line1").country("UK").build());

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NOTICE_OF_PROCEEDINGS_TEMPLATE_ID);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithSoleDivorceApplicationDocumentForSoleApplicationWhenRespondentIsOverseas() {

        final CaseData caseData = caseData(SOLE_APPLICATION);
        caseData.getApplicant2().setHomeAddress(AddressGlobalUK.builder().addressLine1("line1").country("France").build());

        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final var result = generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyInteractions(caseData, templateContent, NOTICE_OF_PROCEEDINGS_OVERSEAS_RESP_TEMPLATE_ID);

        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldNotCallDocAssemblyServiceAndReturnCaseDataWithJointApplicationDocumentForJointApplicationWhenRespondentIsNotOverseas() {

        final CaseData caseData = caseData(JOINT_APPLICATION);
        caseData.getApplicant2().setHomeAddress(AddressGlobalUK.builder().addressLine1("line1").country("UK").build());

        generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyNoInteractions(noticeOfProceedingContent);
    }

    @Test
    void shouldCallDocAssemblyServiceAndReturnCaseDataWithJointApplicationDocumentForJointApplicationWhenRespondentIsOverseas() {

        final CaseData caseData = caseData(JOINT_APPLICATION);
        caseData.getApplicant2().setHomeAddress(AddressGlobalUK.builder().addressLine1("line1").country("France").build());

        generateNoticeOfProceeding.apply(caseDetails(caseData));

        verifyNoInteractions(noticeOfProceedingContent);
    }

    private void verifyInteractions(CaseData caseData, Map<String, Object> templateContent, String templateId) {
        verify(caseDataDocumentService)
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

    private CaseData caseData(ApplicationType applicationType) {
        return CaseData.builder()
            .applicationType(applicationType)
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .application(Application.builder()
                .solSignStatementOfTruth(NO)
                .build())
            .build();
    }

}
