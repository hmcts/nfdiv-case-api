package uk.gov.hmcts.divorce.bulkaction.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithPronouncementJudge.SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
public class BulkCaseCaseTaskFactoryIT {

    @Autowired
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Test
    void shouldReturnUpdateCasePronouncementJudgeProviderCaseTask() {

        final CaseTask caseTask = bulkCaseCaseTaskFactory.getCaseTask(
            mock(BulkActionCaseData.class),
            SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE);

        assertThat(caseTask.getClass().toString()).contains("UpdateCasePronouncementJudgeProvider$$Lambda");
    }

    @Test
    void shouldReturnUpdateCaseCourtHearingProviderCaseTask() {

        final CaseTask caseTask = bulkCaseCaseTaskFactory.getCaseTask(
            mock(BulkActionCaseData.class),
            SYSTEM_UPDATE_CASE_COURT_HEARING);

        assertThat(caseTask.getClass().toString()).contains("UpdateCaseCourtHearingProvider$$Lambda");
    }

    @Test
    void shouldReturnPronounceCaseProviderCaseTask() {

        final CaseTask caseTask = bulkCaseCaseTaskFactory.getCaseTask(
            mock(BulkActionCaseData.class),
            SYSTEM_PRONOUNCE_CASE);

        assertThat(caseTask.getClass().toString()).contains("PronounceCaseProvider$$Lambda");
    }
}
