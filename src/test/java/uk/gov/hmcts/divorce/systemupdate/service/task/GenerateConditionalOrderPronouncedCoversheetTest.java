package uk.gov.hmcts.divorce.systemupdate.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataCOPronounced;

@ExtendWith(MockitoExtension.class)
public class GenerateConditionalOrderPronouncedCoversheetTest {

    @Mock
    private ConditionalOrderPronouncedCoverLetterHelper coverLetterHelper;

    @InjectMocks
    private GenerateConditionalOrderPronouncedCoversheet generateConditionalOrderPronouncedCoversheet;

    @Test
    public void shouldGenerateCoverLettersSoleApplication() {
        CaseData data = buildCaseDataCOPronounced(YES, PRIVATE, PRIVATE);
        data.setApplicationType(SOLE_APPLICATION);

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(data)
            .build();

        generateConditionalOrderPronouncedCoversheet.apply(caseDetails);

        verify(coverLetterHelper).generateConditionalOrderPronouncedCoversheet(
            data, caseDetails.getId(), data.getApplicant1(), CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1);

        verify(coverLetterHelper).generateConditionalOrderPronouncedCoversheetOfflineRespondent(
            data, caseDetails.getId(), data.getApplicant2(), data.getApplicant1());
    }

    @Test
    public void shouldGenerateCoverLettersJointApplication() {
        CaseData data = buildCaseDataCOPronounced(YES, PRIVATE, PRIVATE);
        data.setApplicationType(JOINT_APPLICATION);

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(data)
            .build();

        generateConditionalOrderPronouncedCoversheet.apply(caseDetails);

        verify(coverLetterHelper).generateConditionalOrderPronouncedCoversheet(
            data, caseDetails.getId(), data.getApplicant1(), CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1);

        verify(coverLetterHelper).generateConditionalOrderPronouncedCoversheet(
            data, caseDetails.getId(), data.getApplicant2(), CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2);
    }

    @Test
    public void shouldNotGenerateCoverLettersForOnlineApplicantsWithContactTypePrivate() {
        CaseData data = buildCaseDataCOPronounced(NO, PRIVATE, PRIVATE);
        data.setApplicationType(JOINT_APPLICATION);

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(data)
            .build();

        generateConditionalOrderPronouncedCoversheet.apply(caseDetails);

        assertThat(data.getApplicant1().getCoPronouncedCoverLetterRegenerated()).isNull();
        assertThat(data.getApplicant2().getCoPronouncedCoverLetterRegenerated()).isNull();

        verifyNoInteractions(coverLetterHelper);
    }
}
