package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemEnableSolicitorSwitchToSoleCO.SYSTEM_ENABLE_SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class SystemEnableSolicitorSwitchToSoleCOTest {

    @InjectMocks
    private SystemEnableSolicitorSwitchToSoleCO systemEnableSolicitorSwitchToSoleCO;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemEnableSolicitorSwitchToSoleCO.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_ENABLE_SWITCH_TO_SOLE_CO);
    }

    @Test
    void shouldSetEnableSolicitorSwitchToSoleCoForApplicant1() {
        CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(YES)
                            .submittedDate(LocalDateTime.now().minusDays(15))
                            .build()
                    )
                    .conditionalOrderApplicant2Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(NO)
                            .build()
                    )
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            systemEnableSolicitorSwitchToSoleCO.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getEnableSolicitorSwitchToSoleCo())
            .isNotNull();
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getEnableSolicitorSwitchToSoleCo())
            .isEqualTo(YES);
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getEnableSolicitorSwitchToSoleCo())
            .isNull();
    }

    @Test
    void shouldSetEnableSolicitorSwitchToSoleCoForApplicant2() {
        CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(NO)
                            .build()
                    )
                    .conditionalOrderApplicant2Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(YES)
                            .submittedDate(LocalDateTime.now().minusDays(15))
                            .build()
                    )
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            systemEnableSolicitorSwitchToSoleCO.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getEnableSolicitorSwitchToSoleCo())
            .isNotNull();
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getEnableSolicitorSwitchToSoleCo())
            .isEqualTo(YES);
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getEnableSolicitorSwitchToSoleCo())
            .isNull();
    }
}
