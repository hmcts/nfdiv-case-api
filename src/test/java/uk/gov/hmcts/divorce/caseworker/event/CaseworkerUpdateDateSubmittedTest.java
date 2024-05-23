package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateDateSubmitted.CASEWORKER_UPDATE_DATE_SUBMITTED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseworkerUpdateDateSubmittedTest {

    @InjectMocks
    private CaseworkerUpdateDateSubmitted caseworkerUpdateDateSubmitted;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUpdateDateSubmitted.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPDATE_DATE_SUBMITTED);
    }

    @Test
    void shouldSetDueDateIfDueDateIsNull() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData
            .builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .application(
                Application
                    .builder()
                    .dateSubmitted(LocalDateTime.now())
                    .build()
            )
            .build();

        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateDateSubmitted
            .aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDueDate()).isNotNull();
    }

    @Test
    void shouldNotSetDueDateIfDueDateIsNotNull() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData
            .builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .application(
                Application
                    .builder()
                    .dateSubmitted(LocalDateTime.now())
                    .build()
            )
            .dueDate(LocalDate.now())
            .build();

        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateDateSubmitted
            .aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDueDate()).isEqualTo(LocalDate.now());
    }
}
