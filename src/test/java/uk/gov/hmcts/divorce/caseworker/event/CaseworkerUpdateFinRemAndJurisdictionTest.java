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
import uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdiction.APPLICANT_CLEAR_FO_PRAYER_CHILDREN_WARNING;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdiction.APPLICANT_CLEAR_FO_PRAYER_THEMSELVES_WARNING;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdiction.APPLICANT_CONFIRM_FO_PRAYER_CHILDREN_WARNING;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdiction.APPLICANT_CONFIRM_FO_PRAYER_THEMSELVES_WARNING;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdiction.CASEWORKER_UPDATE_FIN_REM_AND_JURISDICTION;
import static uk.gov.hmcts.divorce.common.event.RegenerateApplicationDocument.REGENERATE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.FinancialOrdersChild.FINANCIAL_ORDERS_CHILD;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.FinancialOrdersThemselves.FINANCIAL_ORDERS_THEMSELVES;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(MockitoExtension.class)
class CaseworkerUpdateFinRemAndJurisdictionTest {

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private CaseworkerUpdateFinRemAndJurisdiction caseworkerUpdateFinRemAndJurisdiction;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUpdateFinRemAndJurisdiction.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPDATE_FIN_REM_AND_JURISDICTION);
    }

    @Test
    void shouldValidateFOPrayerWhenSetCorrectlyForThemselves() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant1().setFinancialOrdersFor(Set.of(FinancialOrderFor.APPLICANT));
        caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateFinRemAndJurisdiction.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotValidateFOPrayerWhenNotSetAndFOForThemselves() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant1().setFinancialOrdersFor(Set.of(FinancialOrderFor.APPLICANT));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateFinRemAndJurisdiction.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APPLICANT_CONFIRM_FO_PRAYER_THEMSELVES_WARNING));
    }

    @Test
    void shouldNotValidateFOPrayerWhenSetForThemselvesAndNoFO() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateFinRemAndJurisdiction.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APPLICANT_CLEAR_FO_PRAYER_THEMSELVES_WARNING));
    }

    @Test
    void shouldNotValidateFOPrayerWhenSetIncorrectlyForThemselves() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant1().setFinancialOrdersFor(Set.of(FinancialOrderFor.CHILDREN));
        caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateFinRemAndJurisdiction.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(2);
        assertThat(response.getErrors()).isEqualTo(
            List.of(APPLICANT_CLEAR_FO_PRAYER_THEMSELVES_WARNING, APPLICANT_CONFIRM_FO_PRAYER_CHILDREN_WARNING)
        );
    }

    @Test
    void shouldValidateFOPrayerWhenSetCorrectlyForTheChildren() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant1().setFinancialOrdersFor(Set.of(FinancialOrderFor.CHILDREN));
        caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateFinRemAndJurisdiction.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotValidateFOPrayerWhenNotSetAndFOForTheChildren() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant1().setFinancialOrdersFor(Set.of(FinancialOrderFor.CHILDREN));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateFinRemAndJurisdiction.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APPLICANT_CONFIRM_FO_PRAYER_CHILDREN_WARNING));
    }

    @Test
    void shouldNotValidateFOPrayerWhenSetForTheChildrenAndNoFO() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateFinRemAndJurisdiction.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APPLICANT_CLEAR_FO_PRAYER_CHILDREN_WARNING));
    }

    @Test
    void shouldNotValidateFOPrayerWhenSetIncorrectlyForTheChildren() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant1().setFinancialOrdersFor(Set.of(FinancialOrderFor.APPLICANT));
        caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateFinRemAndJurisdiction.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(2);
        assertThat(response.getErrors()).isEqualTo(
            List.of(APPLICANT_CONFIRM_FO_PRAYER_THEMSELVES_WARNING, APPLICANT_CLEAR_FO_PRAYER_CHILDREN_WARNING)
        );
    }

    @Test
    void shouldGenerateD8IfApplicationPreviouslyIssued() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.now());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final User user = new User(TEST_AUTHORIZATION_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SYSTEM_AUTHORISATION_TOKEN);

        caseworkerUpdateFinRemAndJurisdiction.submitted(caseDetails, caseDetails);

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, REGENERATE_APPLICATION, user, TEST_SYSTEM_AUTHORISATION_TOKEN);
    }

    @Test
    void shouldNotGenerateD8IfApplicationNotYetIssued() {
        final CaseData caseData = validCaseDataForIssueApplication();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        caseworkerUpdateFinRemAndJurisdiction.submitted(caseDetails, caseDetails);

        verifyNoInteractions(ccdUpdateService);
    }
}
