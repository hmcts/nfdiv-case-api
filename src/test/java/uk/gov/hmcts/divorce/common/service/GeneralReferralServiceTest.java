package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.task.SetGeneralReferralDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralReferral.CASEWORKER_GENERAL_REFERRAL;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;

@ExtendWith(MockitoExtension.class)
class GeneralReferralServiceTest {

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private SetGeneralReferralDetails setGeneralReferralDetails;

    @InjectMocks
    private GeneralReferralService generalReferralService;

    CaseData caseData;
    CaseDetails<CaseData, State> caseDetails;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        caseDetails = CaseDetails.<CaseData, State>builder().id(new Random().nextLong()).data(caseData).build();
    }

    @Test
    void shouldDoNothingWhenStateIsNotFinalOrderRequested() {

        caseDetails.setState(GeneralApplicationReceived);

        generalReferralService.caseWorkerGeneralReferral(caseDetails);

        verifyNoInteractions(idamService);
        verifyNoInteractions(authTokenGenerator);
        verifyNoInteractions(ccdUpdateService);
        verifyNoInteractions(setGeneralReferralDetails);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"FinalOrderRequested", "RespondentFinalOrderRequested"})
    void shouldDoNothingWhenStateIsFinalOrderRequestedButNotOverdue(State state) {

        caseDetails.setState(state);

        generalReferralService.caseWorkerGeneralReferral(caseDetails);

        verifyNoInteractions(idamService);
        verifyNoInteractions(authTokenGenerator);
        verifyNoInteractions(ccdUpdateService);
        verifyNoInteractions(setGeneralReferralDetails);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {"FinalOrderRequested", "RespondentFinalOrderRequested"})
    void shouldCreateEventWhenStateIsFinalOrderRequestedButAndOverdue(State state) {
        caseDetails.setState(state);
        caseData.setFinalOrder(FinalOrder.builder()
            .isFinalOrderOverdue(YesOrNo.YES)
            .build());

        generalReferralService.caseWorkerGeneralReferral(caseDetails);

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(authTokenGenerator).generate();
        verify(ccdUpdateService).submitEventWithRetry(
            eq(caseDetails.getId().toString()), eq(CASEWORKER_GENERAL_REFERRAL), same(setGeneralReferralDetails), any(), any());
        verifyNoInteractions(setGeneralReferralDetails);
    }
}
