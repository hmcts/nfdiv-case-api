package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;

@ExtendWith(MockitoExtension.class)
class SetPostIssueStateTest {

    @InjectMocks
    private SetPostIssueState setPostIssueState;

    @Test
    void shouldSetStateToAwaitingServiceWhenApplicationTypeIsSoleAndServiceMethodIsSolicitorService() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .serviceMethod(SOLICITOR_SERVICE)
                .build())
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setPostIssueState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingService);
    }

    @Test
    void shouldSetStateToAwaitingServiceWhenApplicationTypeIsSoleAndServiceMethodIsPersonalService() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .serviceMethod(PERSONAL_SERVICE)
                .build())
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setPostIssueState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingService);
    }

    @Test
    void shouldSetStateToAwaitingAosWhenApplicationTypeIsJointAndServiceMethodIsCourtService() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .serviceMethod(COURT_SERVICE)
                .build())
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setPostIssueState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingAos);
    }

    @Test
    void shouldSetStateToHoldingWhenApplicationTypeIsNotSole() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder().build())
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setPostIssueState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(Holding);
    }
}
