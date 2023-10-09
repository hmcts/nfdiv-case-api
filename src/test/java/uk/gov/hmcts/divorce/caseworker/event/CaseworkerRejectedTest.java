package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRejected.CASEWORKER_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerRejectedTest {

    @Mock
    private CcdAccessService caseAccessService;

    @InjectMocks
    private CaseworkerRejected caseworkerRejected;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRejected.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REJECTED);
    }

    @Test
    void shouldSetPreviousState() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(Submitted);
        final CaseData caseData = CaseData.builder().build();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRejected.aboutToSubmit(details, null);

        assertThat(response.getData().getApplication().getPreviousState())
            .isEqualTo(Submitted);
    }

    @Test
    public void shouldUnlinkApplicants() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        var caseData = validApplicant2CaseData();
        caseData.setCaseInvite(new CaseInvite(caseData.getCaseInvite().applicant2InviteEmailAddress(), "12345", "12"));
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        var result = caseworkerRejected.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getCaseInvite().accessCode()).isNull();
        assertThat(result.getData().getCaseInvite().applicant2UserId()).isNull();

        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                CREATOR.getRole(),
                APPLICANT_2.getRole()
            )
        ));
    }

    @Test
    public void shouldRemoveSolicitorOrganisationPolicyForRepresentedApplicants() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        var caseData = validApplicant2CaseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        var result = caseworkerRejected.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getApplicant1().getSolicitor().getOrganisationPolicy()).isNull();
        assertThat(result.getData().getApplicant2().getSolicitor().getOrganisationPolicy()).isNull();

        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                CREATOR.getRole(),
                APPLICANT_2.getRole()
            )
        ));
    }
}
