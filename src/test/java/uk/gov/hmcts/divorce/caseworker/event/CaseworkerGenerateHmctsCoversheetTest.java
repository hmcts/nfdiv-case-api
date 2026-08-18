package uk.gov.hmcts.divorce.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateHmctsCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGenerateHmctsCoversheet.CASEWORKER_GENERATE_HMCTS_COVERSHEET;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGenerateHmctsCoversheet.HMCTS_COVERSHEET_EXISTS_ERROR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerGenerateHmctsCoversheetTest {

    @Mock
    private IdamService idamService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Clock clock;

    @Mock
    private GenerateHmctsCoversheet generateHmctsCoversheet;

    @InjectMocks
    private CaseworkerGenerateHmctsCoversheet caseworkerGenerateHmctsCoversheet;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerGenerateHmctsCoversheet.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_GENERATE_HMCTS_COVERSHEET);
    }

    @Test
    void shouldGenerateHmctsCoversheet() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        when(generateHmctsCoversheet.hasHmctsCoverSheet(caseData)).thenReturn(false);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerGenerateHmctsCoversheet.aboutToSubmit(caseDetails, caseDetails);

        verify(generateHmctsCoversheet).hasHmctsCoverSheet(caseData);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldNotGenerateHmctsCoversheetIfItAlreadyExists() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        when(generateHmctsCoversheet.hasHmctsCoverSheet(caseData)).thenReturn(true);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerGenerateHmctsCoversheet.aboutToSubmit(caseDetails, caseDetails);

        verifyNoMoreInteractions(generateHmctsCoversheet);
        assertThat(response.getErrors()).containsExactly(HMCTS_COVERSHEET_EXISTS_ERROR);
    }
}
