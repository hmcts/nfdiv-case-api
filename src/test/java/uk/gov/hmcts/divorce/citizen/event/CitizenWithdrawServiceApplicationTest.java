package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenWithdrawServiceApplication.CITIZEN_WITHDRAW_SERVICE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CitizenWithdrawServiceApplicationTest {

    @InjectMocks
    private CitizenWithdrawServiceApplication citizenWithdrawServiceApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenWithdrawServiceApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_WITHDRAW_SERVICE_APPLICATION);
    }

    @Test
    void shouldClearAllDataFielsPertainingToServiceApplication() {
        CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .alternativeServiceType(AlternativeServiceType.DEEMED)
                .build())
            .build();

        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenWithdrawServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getAlternativeService().getAlternativeServiceType()).isNull();
    }
}
