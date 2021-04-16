package uk.gov.hmcts.divorce.ccd.event.solicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;
import uk.gov.hmcts.divorce.service.solicitor.SolicitorCreatePetitionService;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.ccd.event.solicitor.SolicitorCreate.SOLICITOR_CREATE;
import static uk.gov.hmcts.divorce.util.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
class SolicitorCreateTest {

    @Mock
    private SolicitorCreatePetitionService solicitorCreatePetitionService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SolicitorCreate solicitorCreate;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        solicitorCreate.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getEventID(), is(SOLICITOR_CREATE));
    }

    @Test
    public void shouldSetDefaultCaseDataValues() {
        final CaseData caseData = caseData();
        caseData.setLanguagePreferenceWelsh(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> result = solicitorCreate.aboutToStart(details);

        assertEquals(result.getData().getLanguagePreferenceWelsh(), NO);
    }

    @Test
    public void shouldPopulateMissingRequirementsFieldsInCaseData() throws Exception {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);

        final String auth = "authorization";
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(auth);

        solicitorCreate.aboutToSubmit(details, details);

        verify(solicitorCreatePetitionService).aboutToSubmit(
            caseData,
            details.getId(),
            auth
        );
    }
}
