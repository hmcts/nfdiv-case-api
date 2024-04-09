package uk.gov.hmcts.divorce.systemupdate.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.common.service.task.SendRegeneratedCOPronouncedCoverLetters;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.systemupdate.service.task.RegenerateConditionalOrderPronouncedCoverLetterOffline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRedoPronouncedCoverLetter.SYSTEM_REDO_PRONOUNCED_COVER_LETTER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SystemRedoPronouncedCoverLetterTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private RegenerateConditionalOrderPronouncedCoverLetterOffline regenerateCoverLetters;

    @Mock
    private SendRegeneratedCOPronouncedCoverLetters sendRegeneratedCoverLetters;

    @InjectMocks
    private SystemRedoPronouncedCoverLetter underTest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        underTest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_REDO_PRONOUNCED_COVER_LETTER);
    }

    @Test
    void shouldRegenerateAndSendCoverLetters() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(regenerateCoverLetters.apply(details)).thenReturn(details);
        when(sendRegeneratedCoverLetters.apply(details)).thenReturn(details);

        underTest.aboutToSubmit(details, details);

        verify(regenerateCoverLetters).apply(details);
        verify(sendRegeneratedCoverLetters).apply(details);
    }
}
