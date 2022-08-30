package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.common.service.task.GenerateConditionalOrderAnswersDocument;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant2SolicitorSwitchedToSoleCo.APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class Applicant2SolicitorSwitchedToSoleCoTest {

    @Mock
    private SwitchToSoleService switchToSoleService;

    @Mock
    private GenerateConditionalOrderAnswersDocument generateConditionalOrderAnswersDocument;

    @InjectMocks
    private Applicant2SolicitorSwitchedToSoleCo applicant2SolicitorSwitchedToSoleCo;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant2SolicitorSwitchedToSoleCo.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO);
    }

    @Test
    void shouldSetApplicationTypeToSole() {
        CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorSwitchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getApplication().getSwitchedToSoleCo()).isEqualTo(YES);
        assertThat(response.getData().getLabelContent().getApplicant2()).isEqualTo("respondent");
        assertThat(response.getData().getConditionalOrder().getSwitchedToSole()).isEqualTo(YES);

        verify(generateConditionalOrderAnswersDocument).apply(caseDetails);
    }
}
