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
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.ServiceApplicationDraftSubmissionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorAmendDeemedServiceApplication.SOLICITOR_AMEND_DEEMED_SERVICE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SolicitorAmendDeemedServiceApplicationTest {

    @InjectMocks
    private SolicitorAmendDeemedServiceApplication solicitorAmendDeemedServiceApplication;

    @Mock
    private ServiceApplicationDraftSubmissionService serviceApplicationBuilderService;

    @Test
    void shouldAddSolicitorAmendDeemedServiceApplicationEventToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorAmendDeemedServiceApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_AMEND_DEEMED_SERVICE_APPLICATION);
    }

    @Test
    void shouldGrantCreateReadUpdateToApplicantSolicitorAndReadOnlyToCaseRoles() {
        ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorAmendDeemedServiceApplication.configure(configBuilder);

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
    void shouldSubmitFromInterimOptionsOnAboutToSubmit() {
        Applicant applicant = Applicant.builder()
            .interimApplicationOptions(InterimApplicationOptions.builder().build())
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .build();

        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorAmendDeemedServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);

        verify(serviceApplicationBuilderService).submitFromInterimOptions(TEST_CASE_ID, caseData, applicant);
    }

    @Test
    void shouldReturnErrorInAboutToStartCallbackWhenPaymentReferenceIsPresent() {
        final CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .servicePaymentFee(FeeDetails.builder()
                    .paymentReference("123456")
                    .build())
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorAmendDeemedServiceApplication.aboutToStart(details);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).containsExactly(
            "The ongoing service application on this case has already been submitted and you cannot submit it again or amend it."
        );
    }

    @Test
    void shouldReturnErrorInAboutToStartCallbackWhenHWFReferenceIsPresent() {
        final CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .servicePaymentFee(FeeDetails.builder()
                    .helpWithFeesReferenceNumber("123456")
                    .build())
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorAmendDeemedServiceApplication.aboutToStart(details);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).containsExactly(
            "The ongoing service application on this case has already been submitted and you cannot submit it again or amend it."
        );
    }

    @Test
    void shouldNotReturnErrorInAboutToStartCallback() {
        final CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .servicePaymentFee(FeeDetails.builder()
                    .build())
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = solicitorAmendDeemedServiceApplication.aboutToStart(details);
        assertThat(response.getErrors()).isNull();
    }
}
