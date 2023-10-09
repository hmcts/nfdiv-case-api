package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetServiceTypeTest {

    @InjectMocks
    private SetServiceType setServiceType;

    @Test
    void shouldSetServiceTypeToPersonalServiceIfApplicant2NotRepresentedAndApplicant2IsOverseas() {

        final CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("France").build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> response = setServiceType.apply(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplicant2().setSolicitorRepresented(NO);
        expectedCaseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("France").build());
        expectedCaseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        assertThat(response.getData().getApplicant2()).isEqualTo(expectedCaseData.getApplicant2());
        assertThat(response.getData().getApplication()).isEqualTo(expectedCaseData.getApplication());
    }

    @Test
    void shouldSetServiceTypeToCourtServiceForJointCasesEvenIfApplicant2IsOverseas() {

        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("France").build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> response = setServiceType.apply(caseDetails);

        assertThat(response.getData().getApplication().getServiceMethod()).isEqualTo(COURT_SERVICE);
    }

    @Test
    void shouldNotSetServiceTypeToPersonalServiceIfApplicant2Represented() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("France").build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> response = setServiceType.apply(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplicant1().setSolicitorRepresented(NO);
        expectedCaseData.getApplicant2().setSolicitorRepresented(YES);
        expectedCaseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("France").build());
        expectedCaseData.getApplication().setServiceMethod(COURT_SERVICE);

        assertThat(response.getData().getApplicant1()).isEqualTo(expectedCaseData.getApplicant1());
        assertThat(response.getData().getApplicant2()).isEqualTo(expectedCaseData.getApplicant2());
        assertThat(response.getData().getApplication()).isEqualTo(expectedCaseData.getApplication());
    }

    @Test
    void shouldNotSetServiceTypeToPersonalServiceIfApplicant2NotOverseas() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("UK").build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> response = setServiceType.apply(caseDetails);

        var expectedCaseData = caseData();
        expectedCaseData.getApplicant1().setSolicitorRepresented(NO);
        expectedCaseData.getApplicant2().setSolicitorRepresented(NO);
        expectedCaseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("UK").build());
        expectedCaseData.getApplication().setServiceMethod(COURT_SERVICE);

        assertThat(response.getData().getApplicant1()).isEqualTo(expectedCaseData.getApplicant1());
        assertThat(response.getData().getApplicant2()).isEqualTo(expectedCaseData.getApplicant2());
        assertThat(response.getData().getApplication()).isEqualTo(expectedCaseData.getApplication());
    }
}
