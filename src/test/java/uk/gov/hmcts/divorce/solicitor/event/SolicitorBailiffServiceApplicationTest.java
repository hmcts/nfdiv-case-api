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
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.ServiceApplicationDraftSubmissionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorBailiffServiceApplication.SOLICITOR_BAILIFF_SERVICE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicantWithAddress;

@ExtendWith(MockitoExtension.class)
class SolicitorBailiffServiceApplicationTest {

    @InjectMocks
    private SolicitorBailiffServiceApplication solicitorBailiffServiceApplication;

    @Mock
    private ServiceApplicationDraftSubmissionService serviceApplicationDraftSubmissionService;

    @Test
    void shouldAddSolicitorBailiffServiceApplicationEventToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorBailiffServiceApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_BAILIFF_SERVICE_APPLICATION);
    }

    @Test
    void shouldGrantCreateReadUpdateToApplicantSolicitorAndReadOnlyToCaseRoles() {
        ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorBailiffServiceApplication.configure(configBuilder);

        SetMultimap<UserRole, Permission> expectedRolesAndPermissions = ImmutableSetMultimap.<UserRole, Permission>builder()
            .put(APPLICANT_1_SOLICITOR, C)
            .put(APPLICANT_1_SOLICITOR, R)
            .put(APPLICANT_1_SOLICITOR, U)
            .put(CASE_WORKER, R)
            .put(SUPER_USER, R)
            .put(LEGAL_ADVISOR, R)
            .put(JUDGE, R)
            .build();

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getGrants)
            .containsExactly(expectedRolesAndPermissions);
    }

    @Test
    void shouldSetRespondentNonConfidentialAddressWhenContactDetailsTypeIsPublic() {
        Applicant applicant = getApplicantWithAddress(MALE);
        applicant.setContactDetailsType(PUBLIC);
        CaseData caseData = caseData();
        caseData.setApplicant2(applicant);

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorBailiffServiceApplication.aboutToStart(caseDetails);

        assertThat(response.getData().getApplicant2().getNonConfidentialAddress()).isEqualTo(applicant.getAddress());
    }

    @Test
    void shouldNotSetRespondentNonConfidentialAddressWhenContactDetailsTypeIsPrivate() {
        Applicant applicant = getApplicantWithAddress(MALE);
        applicant.setContactDetailsType(PRIVATE);
        CaseData caseData = caseData();
        caseData.setApplicant2(applicant);

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorBailiffServiceApplication.aboutToStart(caseDetails);

        assertThat(response.getData().getApplicant2().getNonConfidentialAddress()).isNull();
    }

    @Test
    void shouldSetInterimApplicationTypeToBailiffAndSubmitFromInterimOptionsOnAboutToSubmit() {
        InterimApplicationOptions options = InterimApplicationOptions.builder().build();
        Applicant applicant = Applicant.builder()
            .interimApplicationOptions(options)
            .build();
        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .build();

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorBailiffServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(options.getInterimApplicationType()).isEqualTo(InterimApplicationType.BAILIFF_SERVICE);
        assertThat(response.getData()).isEqualTo(caseData);
        verify(serviceApplicationDraftSubmissionService).submitFromInterimOptions(TEST_CASE_ID, caseData, applicant);
    }
}
