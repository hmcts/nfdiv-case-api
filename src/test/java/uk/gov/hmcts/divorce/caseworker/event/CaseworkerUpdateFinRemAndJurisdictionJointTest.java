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
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdictionJoint.APPLICANT_1_CLEAR_FO_PRAYER_CHILDREN_WARNING;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdictionJoint.APPLICANT_1_CLEAR_FO_PRAYER_THEMSELVES_WARNING;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdictionJoint.APPLICANT_1_CONFIRM_FO_PRAYER_CHILDREN_WARNING;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdictionJoint.APPLICANT_1_CONFIRM_FO_PRAYER_THEMSELVES_WARNING;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdictionJoint.APPLICANT_2_CLEAR_FO_PRAYER_CHILDREN_WARNING;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdictionJoint.APPLICANT_2_CLEAR_FO_PRAYER_THEMSELVES_WARNING;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdictionJoint.APPLICANT_2_CONFIRM_FO_PRAYER_CHILDREN_WARNING;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdictionJoint.APPLICANT_2_CONFIRM_FO_PRAYER_THEMSELVES_WARNING;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateFinRemAndJurisdictionJoint.CASEWORKER_UPDATE_FIN_REM_AND_JURISDICTION_JOINT;
import static uk.gov.hmcts.divorce.common.event.RegenerateApplicationDocument.REGENERATE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.FinancialOrdersChild.FINANCIAL_ORDERS_CHILD;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.FinancialOrdersThemselves.FINANCIAL_ORDERS_THEMSELVES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(MockitoExtension.class)
class CaseworkerUpdateFinRemAndJurisdictionJointTest {

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private CaseworkerUpdateFinRemAndJurisdictionJoint caseworkerUpdateFinRemAndJurisdictionJoint;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUpdateFinRemAndJurisdictionJoint.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPDATE_FIN_REM_AND_JURISDICTION_JOINT);
    }

    @Test
    void shouldValidateApp1FOPrayerForWhenSetCorrectlyForThemselves() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant1().setFinancialOrdersFor(Set.of(FinancialOrderFor.APPLICANT));
        caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotValidateApp1FOPrayerWhenNotSetAndFOForThemselves() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant1().setFinancialOrdersFor(Set.of(FinancialOrderFor.APPLICANT));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APPLICANT_1_CONFIRM_FO_PRAYER_THEMSELVES_WARNING));
    }

    @Test
    void shouldNotValidateApp1FOPrayerWhenSetForThemselvesAndNoFO() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APPLICANT_1_CLEAR_FO_PRAYER_THEMSELVES_WARNING));
    }

    @Test
    void shouldNotValidateApp1FOPrayerWhenSetIncorrectlyForThemselves() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant1().setFinancialOrdersFor(Set.of(FinancialOrderFor.CHILDREN));
        caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(2);
        assertThat(response.getErrors()).isEqualTo(
            List.of(APPLICANT_1_CLEAR_FO_PRAYER_THEMSELVES_WARNING, APPLICANT_1_CONFIRM_FO_PRAYER_CHILDREN_WARNING)
        );
    }

    @Test
    void shouldValidateApp1FOPrayerWhenSetCorrectlyForTheChildren() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant1().setFinancialOrdersFor(Set.of(FinancialOrderFor.CHILDREN));
        caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotValidateApp1FOPrayerWhenNotSetAndFOForTheChildren() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant1().setFinancialOrdersFor(Set.of(FinancialOrderFor.CHILDREN));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APPLICANT_1_CONFIRM_FO_PRAYER_CHILDREN_WARNING));
    }

    @Test
    void shouldNotValidateApp1FOPrayerWhenSetForTheChildrenAndNoFO() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APPLICANT_1_CLEAR_FO_PRAYER_CHILDREN_WARNING));
    }

    @Test
    void shouldNotValidateApp1FOPrayerWhenSetIncorrectlyForTheChildren() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setFinancialOrder(YES);
        caseData.getApplicant1().setFinancialOrdersFor(Set.of(FinancialOrderFor.APPLICANT));
        caseData.getApplicant1().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(2);
        assertThat(response.getErrors()).isEqualTo(
            List.of(APPLICANT_1_CONFIRM_FO_PRAYER_THEMSELVES_WARNING, APPLICANT_1_CLEAR_FO_PRAYER_CHILDREN_WARNING)
        );
    }

    @Test
    void shouldValidateApp2FOPrayerForWhenSetCorrectlyForThemselves() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setFinancialOrder(YES);
        caseData.getApplicant2().setFinancialOrdersFor(Set.of(FinancialOrderFor.APPLICANT));
        caseData.getApplicant2().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotValidateApp2FOPrayerWhenNotSetAndFOForThemselves() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setFinancialOrder(YES);
        caseData.getApplicant2().setFinancialOrdersFor(Set.of(FinancialOrderFor.APPLICANT));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APPLICANT_2_CONFIRM_FO_PRAYER_THEMSELVES_WARNING));
    }

    @Test
    void shouldNotValidateApp2FOPrayerWhenSetForThemselvesAndNoFO() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setFinancialOrder(NO);
        caseData.getApplicant2().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APPLICANT_2_CLEAR_FO_PRAYER_THEMSELVES_WARNING));
    }

    @Test
    void shouldNotValidateApp2FOPrayerWhenSetIncorrectlyForThemselves() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setFinancialOrder(YES);
        caseData.getApplicant2().setFinancialOrdersFor(Set.of(FinancialOrderFor.CHILDREN));
        caseData.getApplicant2().getApplicantPrayer().setPrayerFinancialOrdersThemselves(Set.of(FINANCIAL_ORDERS_THEMSELVES));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(2);
        assertThat(response.getErrors()).isEqualTo(
            List.of(APPLICANT_2_CLEAR_FO_PRAYER_THEMSELVES_WARNING, APPLICANT_2_CONFIRM_FO_PRAYER_CHILDREN_WARNING)
        );
    }

    @Test
    void shouldValidateApp2FOPrayerWhenSetCorrectlyForTheChildren() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setFinancialOrder(YES);
        caseData.getApplicant2().setFinancialOrdersFor(Set.of(FinancialOrderFor.CHILDREN));
        caseData.getApplicant2().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotValidateApp2FOPrayerWhenNotSetAndFOForTheChildren() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setFinancialOrder(YES);
        caseData.getApplicant2().setFinancialOrdersFor(Set.of(FinancialOrderFor.CHILDREN));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APPLICANT_2_CONFIRM_FO_PRAYER_CHILDREN_WARNING));
    }

    @Test
    void shouldNotValidateApp2FOPrayerWhenSetForTheChildrenAndNoFO() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setFinancialOrder(NO);
        caseData.getApplicant2().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors()).isEqualTo(Collections.singletonList(APPLICANT_2_CLEAR_FO_PRAYER_CHILDREN_WARNING));
    }

    @Test
    void shouldNotValidateApp2FOPrayerWhenSetIncorrectlyForTheChildren() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setFinancialOrder(YES);
        caseData.getApplicant2().setFinancialOrdersFor(Set.of(FinancialOrderFor.APPLICANT));
        caseData.getApplicant2().getApplicantPrayer().setPrayerFinancialOrdersChild(Set.of(FINANCIAL_ORDERS_CHILD));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateFinRemAndJurisdictionJoint.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(2);
        assertThat(response.getErrors()).isEqualTo(
            List.of(APPLICANT_2_CONFIRM_FO_PRAYER_THEMSELVES_WARNING, APPLICANT_2_CLEAR_FO_PRAYER_CHILDREN_WARNING)
        );
    }

    @Test
    void shouldGenerateD8IfApplicationPreviouslyIssued() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setIssueDate(LocalDate.now());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final User user = new User(TEST_AUTHORIZATION_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SYSTEM_AUTHORISATION_TOKEN);

        caseworkerUpdateFinRemAndJurisdictionJoint.submitted(caseDetails, caseDetails);

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, REGENERATE_APPLICATION, user, TEST_SYSTEM_AUTHORISATION_TOKEN);
    }

    @Test
    void shouldNotGenerateD8IfApplicationNotYetIssued() {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        caseworkerUpdateFinRemAndJurisdictionJoint.submitted(caseDetails, caseDetails);

        verifyNoInteractions(ccdUpdateService);
    }
}
