package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.common.service.WithdrawCaseService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorWithdrawn.CASE_WITHDRAWN_CONFIRMATION_HEADER;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorWithdrawn.CASE_WITHDRAWN_CONFIRMATION_LABEL;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorWithdrawn.SOLICITOR_WITHDRAWN;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SolicitorWithdrawnTest {
    @Mock
    private WithdrawCaseService withdrawCaseService;

    @InjectMocks
    private SolicitorWithdrawn solicitorWithdrawn;

    @Test
    void configure() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorWithdrawn.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_WITHDRAWN);
    }

    @Test
    void shouldWithdrawCaseByDelegatingToWithdrawCaseService() {
        final var beforeDetails = getCaseDetails();
        final var details = getCaseDetails();

        solicitorWithdrawn.aboutToSubmit(details, beforeDetails);

        verify(withdrawCaseService).withdraw(details);
    }

    @Test
    void shouldReturnConfirmationTextOnSubmission() {
        final var details = getCaseDetails();
        final var beforeDetails = getCaseDetails();

        var result = solicitorWithdrawn.submitted(details, beforeDetails);

        assertThat(result.getConfirmationHeader()).isEqualTo(CASE_WITHDRAWN_CONFIRMATION_HEADER);
        assertThat(result.getConfirmationBody()).isEqualTo(
            String.format(CASE_WITHDRAWN_CONFIRMATION_LABEL)
        );
    }

    private CaseDetails<CaseData, State> getCaseDetails() {
        final var details = new CaseDetails<CaseData, State>();
        final var data = caseData();
        details.setData(data);
        details.setId(TEST_CASE_ID);

        return details;
    }
}
