package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.citizen.event.CitizenLinkApplicant2.CITIZEN_LINK_APPLICANT_2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class CitizenLinkApplicant2Test {

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CitizenLinkApplicant2 citizenLinkApplicant2;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        citizenLinkApplicant2.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId()).isEqualTo(CITIZEN_LINK_APPLICANT_2);
    }

    @Test
    void shouldRemoveAccessCodeAfterLinkingApplication() {
        final CaseData caseData = caseData();
        caseData.setCaseInvite(
            CaseInvite.builder()
                .accessCode("D8BC9AQR")
                .applicant2UserId("Applicant2Id")
                .build());
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION))
            .thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenLinkApplicant2.aboutToSubmit(details, details);

        assertThat(response.getData().getCaseInvite().getAccessCode()).isNull();
        verify(ccdAccessService).linkRespondentToApplication(eq("auth header"), eq(1L), eq("Applicant2Id"));
    }
}
