package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformationHelper.APPLICANT_1;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformationHelper.APPLICANT_2;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformationHelper.FULL_STOP;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformationHelper.NO_VALID_EMAIL_ERROR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformationHelper.SOLICITOR;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformationHelper.THE_APPLICANT;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRequestForInformationHelper.THIS_PARTY;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.OTHER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_OTHER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_OTHER_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_TEXT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
class CaseworkerRequestForInformationHelperTest {

    @InjectMocks
    private CaseworkerRequestForInformationHelper caseworkerRequestForInformationHelper;

    @Test
    void shouldSetPartyToApplicant1WhenNotRepresentedOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(1);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isEqualTo(APPLICANT);
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(caseData.getApplicant1().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(caseData.getApplicant1().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldAddAdditionalRequestToRequestListWhenListIsNotEmpty() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);
        final ListValue<RequestForInformation> request = new ListValue<>();
        request.setValue(caseData.getRequestForInformationList().getRequestForInformation());
        final List<ListValue<RequestForInformation>> requests = new ArrayList<>();
        requests.add(request);
        caseData.getRequestForInformationList().setRequestsForInformation(requests);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(2);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(1).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isEqualTo(APPLICANT);
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(caseData.getApplicant1().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(caseData.getApplicant1().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldSetPartyToApplicant1SolicitorWhenRepresentedOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(1);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isEqualTo(APPLICANT);
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldSetPartyToApplicant1WhenNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(1);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(APPLICANT1);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(caseData.getApplicant1().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(caseData.getApplicant1().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldSetPartyToApplicant1SolicitorWhenRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(1);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(APPLICANT1);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldSetPartyToApplicant2WhenNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(1);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(APPLICANT2);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(caseData.getApplicant2().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(caseData.getApplicant2().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldSetPartyToApplicant2SolicitorWhenRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(1);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(APPLICANT2);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress())
            .isEqualTo(caseData.getApplicant2().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName())
            .isEqualTo(caseData.getApplicant2().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldSetPartiesToApplicantsWhenNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(1);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(BOTH);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(caseData.getApplicant1().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(caseData.getApplicant1().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress())
            .isEqualTo(caseData.getApplicant2().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isEqualTo(caseData.getApplicant2().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldSetPartiesToApplicantSolicitorsWhenRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(1);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(BOTH);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress())
            .isEqualTo(caseData.getApplicant2().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName())
            .isEqualTo(caseData.getApplicant2().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldSetPartiesToApplicant1SolicitorAndApplicant2WhenApplicant1RepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(getApplicant());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(1);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(BOTH);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName())
            .isEqualTo(caseData.getApplicant1().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress())
            .isEqualTo(caseData.getApplicant2().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isEqualTo(caseData.getApplicant2().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldSetPartiesToApplicant1AndApplicant2SolicitorWhenApplicant2RepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(1);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(BOTH);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(caseData.getApplicant1().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(caseData.getApplicant1().getFullName());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress())
            .isEqualTo(caseData.getApplicant2().getSolicitor().getEmail());
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName())
            .isEqualTo(caseData.getApplicant2().getSolicitor().getName());
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldSetPartyToOtherOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationName(TEST_OTHER_NAME);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(TEST_OTHER_EMAIL);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(1);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isEqualTo(OTHER);
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(TEST_OTHER_EMAIL);
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(TEST_OTHER_NAME);
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldSetPartyToOtherOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation()
            .setRequestForInformationJointParties(RequestForInformationJointParties.OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationName(TEST_OTHER_NAME);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(TEST_OTHER_EMAIL);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationDetails(TEST_TEXT);

        CaseData response = caseworkerRequestForInformationHelper.setParties(caseData);
        assertThat(response.getRequestForInformationList().getRequestsForInformation().size()).isEqualTo(1);

        RequestForInformation responseRequestForInformation =
            response.getRequestForInformationList().getRequestsForInformation().get(0).getValue();

        assertThat(responseRequestForInformation.getRequestForInformationSoleParties()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationJointParties()).isEqualTo(RequestForInformationJointParties.OTHER);
        assertThat(responseRequestForInformation.getRequestForInformationEmailAddress()).isEqualTo(TEST_OTHER_EMAIL);
        assertThat(responseRequestForInformation.getRequestForInformationName()).isEqualTo(TEST_OTHER_NAME);
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryEmailAddress()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationSecondaryName()).isNull();
        assertThat(responseRequestForInformation.getRequestForInformationDetails()).isEqualTo(TEST_TEXT);
    }

    @Test
    void shouldValidateApplicantEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNoApplicantEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + THE_APPLICANT + FULL_STOP);
    }

    @Test
    void shouldValidateApplicantSolicitorEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNoApplicantSolicitorEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().getSolicitor().setEmail("");
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + THE_APPLICANT + SOLICITOR);
    }

    @Test
    void shouldValidateOtherEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(TEST_USER_EMAIL);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNoOtherEmailOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + THIS_PARTY);
    }

    @Test
    void shouldValidateApplicant1EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNoApplicant1EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + APPLICANT_1 + FULL_STOP);
    }

    @Test
    void shouldValidateApplicant1SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNoApplicant1SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().getSolicitor().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + APPLICANT_1 + SOLICITOR);
    }

    @Test
    void shouldValidateApplicant2EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNoApplicant2EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + APPLICANT_2 + FULL_STOP);
    }

    @Test
    void shouldValidateApplicant2SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNoApplicant2SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().getSolicitor().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + APPLICANT_2 + SOLICITOR);
    }

    @Test
    void shouldValidateBothApplicantEmailsOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNoApplicantEmailsOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant1().setEmail("");
        caseData.getApplicant2().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + APPLICANT_1 + FULL_STOP, NO_VALID_EMAIL_ERROR + APPLICANT_2 + FULL_STOP);
    }

    @Test
    void shouldReturnErrorWhenApplicant1EmailAndNoApplicant2EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant2().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + APPLICANT_2 + FULL_STOP);
    }

    @Test
    void shouldReturnErrorWhenApplicant2EmailAndNoApplicant1EmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant1().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + APPLICANT_1 + FULL_STOP);
    }

    @Test
    void shouldValidateApplicant1SolicitorAndApplicant2SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setGender(MALE);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNoApplicantSolicitorEmailsOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant1().getSolicitor().setEmail("");
        caseData.getApplicant2().getSolicitor().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + APPLICANT_1 + SOLICITOR, NO_VALID_EMAIL_ERROR + APPLICANT_2 + SOLICITOR);
    }

    @Test
    void shouldReturnErrorWhenNoApplicant1SolicitorEmailAndApplicant2SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant1().getSolicitor().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + APPLICANT_1 + SOLICITOR);
    }

    @Test
    void shouldReturnErrorWhenNoApplicant2SolicitorEmailAndApplicant1SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant2().getSolicitor().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + APPLICANT_2 + SOLICITOR);
    }

    @Test
    void shouldReturnErrorWhenNoApplicant1EmailAndNoApplicant2SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(getApplicant());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant1().setEmail("");
        caseData.getApplicant2().getSolicitor().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + APPLICANT_1 + FULL_STOP, NO_VALID_EMAIL_ERROR + APPLICANT_2 + SOLICITOR);
    }

    @Test
    void shouldReturnErrorWhenNoApplicant2EmailAndNoApplicant1SolicitorEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setGender(MALE);
        caseData.getApplicant1().getSolicitor().setEmail("");
        caseData.getApplicant2().setEmail("");
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + APPLICANT_1 + SOLICITOR, NO_VALID_EMAIL_ERROR + APPLICANT_2 + FULL_STOP);
    }

    @Test
    void shouldValidateOtherEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationEmailAddress(TEST_USER_EMAIL);

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNoOtherEmailOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(
            RequestForInformationJointParties.OTHER
        );

        List<String> response = caseworkerRequestForInformationHelper.areEmailsValid(caseData);

        assertThat(response).contains(NO_VALID_EMAIL_ERROR + THIS_PARTY);
    }
}
