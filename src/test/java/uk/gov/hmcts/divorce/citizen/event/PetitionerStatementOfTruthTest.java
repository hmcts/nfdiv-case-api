package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.PetitionerStatementOfTruth.PETITIONER_STATEMENT_OF_TRUTH;

@ExtendWith(MockitoExtension.class)
class PetitionerStatementOfTruthTest {

    @InjectMocks
    private PetitionerStatementOfTruth petitionerStatementOfTruth;

    @Mock
    private State awaitingPayment;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        petitionerStatementOfTruth.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getEventID(), is(PETITIONER_STATEMENT_OF_TRUTH));
    }

    @Test
    public void givenEventStartedThenValidateCase() throws Exception {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        List<String> validationErrors = new ArrayList<>();
        validationErrors.add("Some error");

        when(awaitingPayment.validate(caseData)).thenReturn(validationErrors);
        final AboutToStartOrSubmitResponse<CaseData, State> response = petitionerStatementOfTruth.aboutToStart(caseDetails);

//        verify(awaitingPayment, 1).validate(caseData);
    }
}
