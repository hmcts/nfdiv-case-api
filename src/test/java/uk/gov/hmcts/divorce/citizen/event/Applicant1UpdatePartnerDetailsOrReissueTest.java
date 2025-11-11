package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.task.SetPostIssueState;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponseJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponsePartnerNewEmailOrAddress;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponseSendPapersAgainOrTrySomethingElse;
import uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerReissueApplication.CASEWORKER_REISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.citizen.event.Applicant1UpdatePartnerDetailsOrReissue.CONFIDENTIAL_RESPONDENT_ERROR;
import static uk.gov.hmcts.divorce.citizen.event.Applicant1UpdatePartnerDetailsOrReissue.UPDATE_PARTNER_DETAILS_OR_REISSUE;
import static uk.gov.hmcts.divorce.divorcecase.model.NoResponsePartnerNewEmailOrAddress.CONTACT_DETAILS_UPDATED;
import static uk.gov.hmcts.divorce.divorcecase.model.NoResponseSendPapersAgainOrTrySomethingElse.PAPERS_SENT;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.SERVICE_DOCUMENTS_ALREADY_REGENERATED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForReIssueApplication;

@ExtendWith(MockitoExtension.class)
class Applicant1UpdatePartnerDetailsOrReissueTest {

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private SetPostIssueState setPostIssueState;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private Applicant1UpdatePartnerDetailsOrReissue applicant1UpdatePartnerDetailsOrReissue;

    private static final int REISSUE_OFFSET_DAYS = 14;

    @BeforeEach
    void setOffsetDays() {
        ReflectionTestUtils.setField(
            applicant1UpdatePartnerDetailsOrReissue,
            "docsRegeneratedOffsetDays",
            REISSUE_OFFSET_DAYS
        );
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant1UpdatePartnerDetailsOrReissue.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(UPDATE_PARTNER_DETAILS_OR_REISSUE);
    }

    @Test
    void shouldRejectTheUpdateIfServiceDateValidationFails() {
        final CaseData caseData = validCaseDataForReIssueApplication();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        try (MockedStatic<ApplicationValidation> classMock = Mockito.mockStatic(ApplicationValidation.class)) {
            classMock.when(() -> ApplicationValidation.validateServiceDate(caseData, REISSUE_OFFSET_DAYS))
                .thenReturn(List.of(SERVICE_DOCUMENTS_ALREADY_REGENERATED));

            final AboutToStartOrSubmitResponse<CaseData, State> response =
                applicant1UpdatePartnerDetailsOrReissue.aboutToStart(caseDetails);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).contains(SERVICE_DOCUMENTS_ALREADY_REGENERATED);
        }
    }

    @Test
    void shouldAllowTheUpdateIfServiceDateValidationPasses() {
        final CaseData caseData = validCaseDataForReIssueApplication();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        try (MockedStatic<ApplicationValidation> classMock = Mockito.mockStatic(ApplicationValidation.class)) {
            classMock.when(() -> ApplicationValidation.validateServiceDate(caseData, REISSUE_OFFSET_DAYS))
                .thenReturn(Collections.emptyList());

            final AboutToStartOrSubmitResponse<CaseData, State> response =
                applicant1UpdatePartnerDetailsOrReissue.aboutToStart(caseDetails);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Test
    void shouldRejectTheUpdateIfAosSubmittedByPartner() {
        final CaseData caseData = validCaseDataForReIssueApplication();

        caseData.getAcknowledgementOfService().setDateAosSubmitted(
            LocalDateTime.of(2021, 10, 26, 10, 0, 0));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        try (MockedStatic<ApplicationValidation> classMock = Mockito.mockStatic(ApplicationValidation.class)) {
            classMock.when(() -> ApplicationValidation.validateServiceDate(caseData, REISSUE_OFFSET_DAYS))
                .thenReturn(Collections.emptyList());

            final AboutToStartOrSubmitResponse<CaseData, State> response =
                applicant1UpdatePartnerDetailsOrReissue.aboutToStart(caseDetails);

            assertThat(response.getErrors()).hasSize(1);
            assertThat(response.getErrors()).contains("Partner has responded to application.");
        }
    }

    @Test
    void shouldRejectTheUpdateIfRespondentIsConfidentialAndPersonalServiceIsRequired() {
        final CaseData caseData = validCaseDataForReIssueApplication();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
            .noResponseJourneyOptions(NoResponseJourneyOptions.builder()
                .noResponsePartnerNewEmailOrAddress(NoResponsePartnerNewEmailOrAddress.EMAIL_AND_ADDRESS)
                .noResponsePartnerAddressOverseas(YesOrNo.YES)
                .build())
            .build());
        caseData.getApplicant2().setContactDetailsType(ContactDetailsType.PRIVATE);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1UpdatePartnerDetailsOrReissue.aboutToSubmit(caseDetails, null);


        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(CONFIDENTIAL_RESPONDENT_ERROR);
    }

    @Test
    void shouldSetNewEmailForRespondentAboutToSubmitWhenNoResponsePartnerNewEmailOrAddressIsEmail() {
        final CaseData caseData = validCaseDataForReIssueApplication();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
            .noResponseJourneyOptions(NoResponseJourneyOptions.builder()
                .noResponsePartnerNewEmailOrAddress(NoResponsePartnerNewEmailOrAddress.EMAIL)
                .noResponsePartnerAddressOverseas(YesOrNo.NO)
                .build())
            .build());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1UpdatePartnerDetailsOrReissue.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getApplicant1().getInterimApplicationOptions()
                .getNoResponseJourneyOptions()).isNotNull();
        assertThat(response.getErrors()).isNull();
        assertThat(caseData.getApplicant1().getInterimApplicationOptions().getNoResponseJourneyOptions()
            .getNoResponsePartnerNewEmailOrAddress()).isEqualTo(CONTACT_DETAILS_UPDATED);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldSetNewAddressForRespondentAboutToSubmitWhenNoResponsePartnerNewEmailOrAddressIsAddress() {
        final CaseData caseData = validCaseDataForReIssueApplication();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
            .noResponseJourneyOptions(NoResponseJourneyOptions.builder()
                .noResponsePartnerNewEmailOrAddress(NoResponsePartnerNewEmailOrAddress.ADDRESS)
                .noResponsePartnerNewEmailOrAddress(NoResponsePartnerNewEmailOrAddress.ADDRESS)
                .noResponsePartnerAddressOverseas(YesOrNo.NO)
                .build())
            .build());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1UpdatePartnerDetailsOrReissue.aboutToSubmit(caseDetails, null);

        assertThat(response.getErrors()).isNull();
        assertThat(caseData.getApplicant1().getInterimApplicationOptions().getNoResponseJourneyOptions()
            .getNoResponsePartnerNewEmailOrAddress()).isEqualTo(CONTACT_DETAILS_UPDATED);
    }

    @Test
    void shouldSetServiceTypeToPersonalServiceWhenPartnerAddressIsOutsideEnglandAndWales() {

        final CaseData caseData = validCaseDataForReIssueApplication();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
            .noResponseJourneyOptions(
                NoResponseJourneyOptions.builder()
                .noResponsePartnerNewEmailOrAddress(NoResponsePartnerNewEmailOrAddress.ADDRESS)
                .noResponsePartnerAddressOverseas(YesOrNo.NO)
                .noResponseRespondentAddressInEnglandWales(YesOrNo.NO)
                .build())
            .build());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1UpdatePartnerDetailsOrReissue.aboutToSubmit(caseDetails, null);

        assertThat(response.getErrors()).isNull();
        assertThat(caseData.getApplication().getServiceMethod()).isEqualTo(ServiceMethod.PERSONAL_SERVICE);
        assertThat(caseData.getApplicant1().getInterimApplicationOptions().getNoResponseJourneyOptions()
            .getNoResponsePartnerNewEmailOrAddress()).isEqualTo(CONTACT_DETAILS_UPDATED);
    }

    @Test
    void shouldSetServiceTypeToCourtServiceWhenPartnerAddressIsInEnglandAndWales() {

        final CaseData caseData = validCaseDataForReIssueApplication();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
            .noResponseJourneyOptions(
                NoResponseJourneyOptions.builder()
                    .noResponsePartnerNewEmailOrAddress(NoResponsePartnerNewEmailOrAddress.ADDRESS)
                    .noResponsePartnerAddressOverseas(YesOrNo.NO)
                    .build())
            .build());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1UpdatePartnerDetailsOrReissue.aboutToSubmit(caseDetails, null);

        assertThat(response.getErrors()).isNull();
        assertThat(caseData.getApplication().getServiceMethod()).isEqualTo(ServiceMethod.COURT_SERVICE);
        assertThat(caseData.getApplicant1().getInterimApplicationOptions().getNoResponseJourneyOptions()
            .getNoResponsePartnerNewEmailOrAddress()).isEqualTo(CONTACT_DETAILS_UPDATED);
    }

    @Test
    void shouldSendPapersAgainToRespondentAboutToSubmitWhenNoResponsePartnerNewEmailOrAddressIsAddress() {
        final CaseData caseData = validCaseDataForReIssueApplication();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
            .noResponseJourneyOptions(NoResponseJourneyOptions.builder()
                .noResponseSendPapersAgainOrTrySomethingElse(NoResponseSendPapersAgainOrTrySomethingElse.SEND_PAPERS_AGAIN)
                .noResponsePartnerAddressOverseas(YesOrNo.NO)
                .build())
            .build());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1UpdatePartnerDetailsOrReissue.aboutToSubmit(caseDetails, null);

        assertThat(response.getErrors()).isNull();
        assertThat(caseData.getApplicant1().getInterimApplicationOptions().getNoResponseJourneyOptions()
            .getNoResponseSendPapersAgainOrTrySomethingElse()).isEqualTo(PAPERS_SENT);
    }

    @Test
    void shouldBlankOutInterimApplicationOptions() {
        final CaseData caseData = validCaseDataForReIssueApplication();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
            .interimApplicationType(InterimApplicationType.PROCESS_SERVER_SERVICE)
            .noResponseJourneyOptions(NoResponseJourneyOptions.builder()
                .noResponseSendPapersAgainOrTrySomethingElse(NoResponseSendPapersAgainOrTrySomethingElse.SEND_PAPERS_AGAIN)
                .noResponsePartnerAddressOverseas(YesOrNo.NO)
                .build())
            .build());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1UpdatePartnerDetailsOrReissue.aboutToSubmit(caseDetails, null);

        assertThat(response.getErrors()).isNull();
        assertThat(caseData.getApplicant1().getInterimApplicationOptions().getInterimApplicationType())
            .isNull();
    }

    @Test
    void shouldTriggerEventForReissueApplicationWhenApplicantUpdateContactDetailsForPartner() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
            .noResponseJourneyOptions(NoResponseJourneyOptions.builder()
                .noResponsePartnerNewEmailOrAddress(NoResponsePartnerNewEmailOrAddress.EMAIL_AND_ADDRESS)
                .noResponsePartnerAddressOverseas(YesOrNo.NO)
                .build())
            .build());
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        var user = mock(User.class);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        applicant1UpdatePartnerDetailsOrReissue.submitted(caseDetails, beforeDetails);

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, CASEWORKER_REISSUE_APPLICATION, user, TEST_SERVICE_AUTH_TOKEN);
    }
}
