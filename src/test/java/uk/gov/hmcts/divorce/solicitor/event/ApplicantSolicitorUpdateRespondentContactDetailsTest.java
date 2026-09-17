package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.solicitor.event.ApplicantSolicitorUpdateRespondentContactDetails.APP_SOLICITOR_UPDATE_RESPONDENT_DETAILS;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicantWithAddress;


@ExtendWith(MockitoExtension.class)
class ApplicantSolicitorUpdateRespondentContactDetailsTest {

    @InjectMocks
    private ApplicantSolicitorUpdateRespondentContactDetails applicantSolicitorUpdateRespondentContactDetails;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicantSolicitorUpdateRespondentContactDetails.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APP_SOLICITOR_UPDATE_RESPONDENT_DETAILS);
    }

    @Test
    void aboutToStartShouldReturnValidationErrorIfAosIsSubmitted() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(Holding);
        final CaseData caseData = CaseData.builder()
            .acknowledgementOfService(AcknowledgementOfService.builder().dateAosSubmitted(LocalDateTime.now()).build())
            .build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicantSolicitorUpdateRespondentContactDetails.aboutToStart(details);

        assertThat(response.getErrors()).containsExactly(
            "You cannot use this event at this stage of the case.");
    }

    @Test
    void aboutToStartShouldReturnValidationErrorIfCOIsSubmitted() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(Holding);
        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .submittedDate(LocalDateTime.now())
                    .build())
                .build())
            .build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicantSolicitorUpdateRespondentContactDetails.aboutToStart(details);

        assertThat(response.getErrors()).containsExactly(
            "You cannot use this event at this stage of the case.");
    }

    @Test
    void aboutToStartShouldReturnValidationErrorIfRespondentIsConfidential() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(Holding);
        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicantSolicitorUpdateRespondentContactDetails.aboutToStart(details);

        assertThat(response.getErrors()).containsExactly(
            "You cannot use this event to update the respondent's contact details as they have been marked as confidential.");
    }

    @Test
    void aboutToStartShouldNotReturnValidationErrorIfAosIsNotSubmitted() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(Submitted);
        final CaseData caseData = CaseData.builder()
            .acknowledgementOfService(AcknowledgementOfService.builder().build())
            .build();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicantSolicitorUpdateRespondentContactDetails.aboutToStart(details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void aboutToStartShouldCopyAddressAndEmailDataToNonConfidentialFields() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(Submitted);

        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1("line 1")
            .postTown("town")
            .postCode("postcode")
            .country("UK")
            .build();

        Applicant respondent = getApplicantWithAddress(Gender.MALE);

        final CaseData caseData = CaseData.builder()
            .acknowledgementOfService(AcknowledgementOfService.builder().build())
            .applicant2(respondent)
            .build();

        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicantSolicitorUpdateRespondentContactDetails.aboutToStart(details);

        assertThat(response.getData().getApplicant2().getNonConfidentialAddress()).isEqualTo(addressGlobalUK);
        assertThat(response.getData().getApplicant2().getNonConfidentialEmail()).isEqualTo(respondent.getEmail());
    }

    @Test
    void aboutToSubmitShouldReturnErrorIfEmailIsRemoved() {
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1("line 1")
            .postTown("town")
            .postCode("postcode")
            .country("UK")
            .build();

        Applicant beforeRespondent = getApplicantWithAddress(Gender.MALE);
        beforeRespondent.setEmail(TEST_USER_EMAIL);
        Applicant afterRespondent = getApplicantWithAddress(Gender.MALE);
        afterRespondent.setNonConfidentialEmail(null);

        final CaseData beforeCaseData = CaseData.builder()
            .applicant2(beforeRespondent)
            .build();

        final CaseData afterCaseData = CaseData.builder()
            .applicant2(afterRespondent)
            .build();

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setState(Submitted);
        beforeDetails.setData(beforeCaseData);

        final CaseDetails<CaseData, State> afterDetails = new CaseDetails<>();
        afterDetails.setState(Submitted);
        afterDetails.setData(afterCaseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            applicantSolicitorUpdateRespondentContactDetails.aboutToSubmit(afterDetails, beforeDetails);

        assertThat(response.getErrors()).containsExactly("You cannot remove the respondent's email address using this event.");
    }

    @Test
    void aboutToSubmitShouldUpdateRespondentDetails() {
        AddressGlobalUK addressGlobalUK = AddressGlobalUK.builder()
            .addressLine1("line 1")
            .postTown("town")
            .postCode("postcode")
            .country("UK")
            .build();

        Applicant beforeRespondent = getApplicant(Gender.MALE);
        Applicant afterRespondent = getApplicant(Gender.MALE);
        afterRespondent.setNonConfidentialAddress(addressGlobalUK);
        afterRespondent.setNonConfidentialEmail(TEST_USER_EMAIL);

        final CaseData beforeCaseData = CaseData.builder()
            .applicant2(beforeRespondent)
            .build();

        final CaseData afterCaseData = CaseData.builder()
            .applicant2(afterRespondent)
            .build();

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setState(Submitted);
        beforeDetails.setData(beforeCaseData);

        final CaseDetails<CaseData, State> afterDetails = new CaseDetails<>();
        afterDetails.setState(Submitted);
        afterDetails.setData(afterCaseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            applicantSolicitorUpdateRespondentContactDetails.aboutToSubmit(afterDetails, beforeDetails);

        assertThat(response.getData().getApplicant2().getAddress()).isEqualTo(addressGlobalUK);
        assertThat(response.getData().getApplicant2().getEmail()).isEqualTo(TEST_USER_EMAIL);
    }
}
