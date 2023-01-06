package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SetConfirmServiceStateTest {

    @InjectMocks
    private SetConfirmServiceState setConfirmServiceState;

    @Test
    void shouldSetStateToHoldingWhenServiceProcessedByProcessServer() {
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplication(
            Application.builder()
                .solicitorService(SolicitorService.builder()
                    .serviceProcessedByProcessServer(Set.of(SolicitorService.ServiceProcessedByProcessServer.CONFIRM))
                    .build())
                .build()
        );
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setState(AwaitingAos);

        final CaseDetails<CaseData, State> result = setConfirmServiceState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(Holding);
    }

    @Test
    void shouldNotSetStateToHoldingWhenServiceNotProcessedByProcessServer() {
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplication(
            Application.builder()
                .solicitorService(SolicitorService.builder()
                    .serviceProcessedByProcessServer(null)
                    .build())
                .build()
        );
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setState(AwaitingAos);

        final CaseDetails<CaseData, State> result = setConfirmServiceState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingAos);
    }

    @Test
    void shouldNotAlterStateWhenSoleApplicationAndAoSSubmitted() {
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplication(new Application());
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setAcknowledgementOfService(AcknowledgementOfService.builder().dateAosSubmitted(LocalDateTime.now()).build());
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setState(Holding);

        final CaseDetails<CaseData, State> result = setConfirmServiceState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(Holding);
    }

    @Test
    void shouldAlterStateWhenJointApplication() {
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplication(new Application());
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setAcknowledgementOfService(AcknowledgementOfService.builder().dateAosSubmitted(LocalDateTime.now()).build());
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setState(Holding);

        final CaseDetails<CaseData, State> result = setConfirmServiceState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingAos);
    }

    @Test
    void shouldAlterStateWhenSoleApplicationAndAoSNotYetSubmitted() {
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplication(new Application());
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setState(Holding);

        final CaseDetails<CaseData, State> result = setConfirmServiceState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingAos);
    }
}
