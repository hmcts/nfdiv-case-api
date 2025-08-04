package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.ReIssueApplicationService;
import uk.gov.hmcts.divorce.citizen.event.Applicant1UpdatePartnerDetailsAndReissue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponseJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.NoResponsePartnerNewEmailOrPostalAddress;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.divorce.systemupdate.service.InvalidReissueOptionException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerReissueApplication.CASEWORKER_REISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.citizen.event.Applicant1UpdatePartnerDetailsAndReissue.UPDATE_PARTNER_DETAILS_AND_REISSUE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForReIssueApplication;

@ExtendWith(MockitoExtension.class)
class Applicant1UpdatePartnerDetailsAndReissueTest {

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private ReIssueApplicationService reIssueApplicationService;


    @InjectMocks
    private Applicant1UpdatePartnerDetailsAndReissue applicant1UpdatePartnerDetailsAndReissue;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant1UpdatePartnerDetailsAndReissue.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(UPDATE_PARTNER_DETAILS_AND_REISSUE);
    }

    @Test
    void shouldReturnErrorForAboutToSubmitIfInvalidReissueOptionExceptionIsThrown() {

        final CaseData caseData = validCaseDataForReIssueApplication();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        doThrow(new InvalidReissueOptionException("")).when(reIssueApplicationService)
            .updateReissueOptionForNewContactDetails(caseDetails, caseDetails.getId());

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1UpdatePartnerDetailsAndReissue.aboutToSubmit(caseDetails, null);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).isEqualTo("Invalid update contact details option selected for CaseId: 12345");
    }

    @Test
    void shouldNotReturnErrorForAboutToSubmitWhenNoResponseUpdateContactDetailsIsSet() {

        final CaseData caseData = validCaseDataForReIssueApplication();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions.builder()
            .noResponseJourneyOptions(NoResponseJourneyOptions.builder()
                .noResponsePartnerNewEmailOrPostalAddress(NoResponsePartnerNewEmailOrPostalAddress.NEW_EMAIL_ADDRESS)
                .noResponsePartnerAddressOverseas(YesOrNo.NO)
                .build())
            .build());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1UpdatePartnerDetailsAndReissue.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getApplicant1().getInterimApplicationOptions().getNoResponseJourneyOptions()).isNull();

        verify(reIssueApplicationService).updateReissueOptionForNewContactDetails(caseDetails, caseDetails.getId());
    }

    @Test
    void shouldTriggerEventForReissueApplicationWhenApplicantUpdateContactDetailsForPartner() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        var user = mock(User.class);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        applicant1UpdatePartnerDetailsAndReissue.submitted(caseDetails, beforeDetails);

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, CASEWORKER_REISSUE_APPLICATION, user, TEST_SERVICE_AUTH_TOKEN);
    }
}
