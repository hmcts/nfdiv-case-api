package uk.gov.hmcts.divorce.solicitor.event;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.GeneralApplicationDraftJourneyService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.D;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorDraftGeneralApplicationApplicant1.SOLICITOR_DRAFT_GEN_APP_APPLICANT1;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SolicitorDraftGeneralApplicationApplicant1Test {

    @InjectMocks
    private SolicitorDraftGeneralApplicationApplicant1 solicitorDraftGeneralApplicationApplicant1;

    @Mock
    private GeneralApplicationDraftJourneyService generalApplicationDraftJourneyService;

    @Test
    void shouldAddEventToConfigBuilder() {
        ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorDraftGeneralApplicationApplicant1.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_DRAFT_GEN_APP_APPLICANT1);
    }

    @Test
    void shouldGrantCrudToApplicant1SolicitorAndReadOnlyToCaseRoles() {
        ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorDraftGeneralApplicationApplicant1.configure(configBuilder);

        SetMultimap<UserRole, Permission> expected = ImmutableSetMultimap.<UserRole, Permission>builder()
            .put(APPLICANT_1_SOLICITOR, C)
            .put(APPLICANT_1_SOLICITOR, R)
            .put(APPLICANT_1_SOLICITOR, U)
            .put(APPLICANT_1_SOLICITOR, D)
            .put(CASE_WORKER, R)
            .put(SUPER_USER, R)
            .put(LEGAL_ADVISOR, R)
            .put(JUDGE, R)
            .build();

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getGrants)
            .containsExactlyInAnyOrder(expected);
    }

    @Test
    void shouldDelegateAboutToStartToJourneyService() {
        Applicant applicant1 = Applicant.builder().build();
        CaseData caseData = CaseData.builder().applicant1(applicant1).build();

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorDraftGeneralApplicationApplicant1.aboutToStart(caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        verify(generalApplicationDraftJourneyService).prepareAboutToStart(caseDetails, applicant1);
    }

    @Test
    void shouldDelegateAboutToSubmitToJourneyService() {
        Applicant applicant1 = Applicant.builder().build();
        CaseData caseData = CaseData.builder().applicant1(applicant1).build();

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorDraftGeneralApplicationApplicant1.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        verify(generalApplicationDraftJourneyService).prepareAboutToSubmit(caseDetails, applicant1);
    }
}
