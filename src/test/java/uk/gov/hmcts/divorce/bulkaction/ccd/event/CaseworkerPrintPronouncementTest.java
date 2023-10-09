package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.PronouncementListTemplateContent;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerPrintPronouncement.CASEWORKER_PRINT_PRONOUNCEMENT;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CaseworkerPrintPronouncementTest {
    @Mock
    private ScheduleCaseService scheduleCaseService;

    @InjectMocks
    private CaseworkerPrintPronouncement printPronounceCase;

    @Mock
    private PronouncementListTemplateContent templateContentService;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private Clock clock;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        printPronounceCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_PRINT_PRONOUNCEMENT);
    }

    @Test
    void shouldSuccessfullyUpdatePronouncementJudgeDetailsForCasesInBulk() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(TEST_CASE_ID);

        doNothing().when(scheduleCaseService).updatePronouncementJudgeDetailsForCasesInBulk(details);

        SubmittedCallbackResponse submittedCallbackResponse = printPronounceCase.submitted(details, details);

        assertThat(submittedCallbackResponse).isNotNull();
        verify(scheduleCaseService).updatePronouncementJudgeDetailsForCasesInBulk(details);
    }

    @Test
    void shouldSetDefaultPronouncementJudgeWhenNullInAboutToStart() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = printPronounceCase.aboutToStart(details);

        assertThat(response.getData().getPronouncementJudge()).isEqualTo("District Judge");
    }

    @Test
    void shouldNotChangePronouncementJudgeWhenSetInAboutToStart() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder()
                            .pronouncementJudge("Judge Bloggs")
                            .build());
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = printPronounceCase.aboutToStart(details);

        assertThat(response.getData().getPronouncementJudge()).isEqualTo("Judge Bloggs");
    }
}
