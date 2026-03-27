package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.NoRespondentAddressJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenAddPartnerContactDetails.CANNOT_UPDATE_PARTNER_DETAILS_ERROR;
import static uk.gov.hmcts.divorce.citizen.event.CitizenAddPartnerContactDetails.CITIZEN_ADD_PARTNER_DETAILS;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(SpringExtension.class)
class CitizenAddPartnerContactDetailsTest {

    @InjectMocks
    private CitizenAddPartnerContactDetails citizenAddPartnerContactDetails;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenAddPartnerContactDetails.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_ADD_PARTNER_DETAILS);
    }

    @Test
    void shouldNotTransitionStateWhenStateIsNotAwaitingDocuments() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        caseDetails.setData(getCaseData(false));
        caseDetails.setState(State.AwaitingHWFDecision);

        var response = citizenAddPartnerContactDetails.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingHWFDecision);
    }

    @Test
    void shouldNotTransitionStateWhenStateIsAwaitingDocumentsButApplicantCouldNotProvideDocuments() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        caseDetails.setData(getCaseData(false));
        caseDetails.setState(State.AwaitingDocuments);

        caseDetails.getData().getApplication().setApplicant1CannotUpload(YesOrNo.YES);

        var response = citizenAddPartnerContactDetails.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingDocuments);
    }

    @Test
    void shouldTransitionToSubmittedState() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        caseDetails.setData(getCaseData(false));
        caseDetails.setState(State.AwaitingDocuments);

        caseDetails.getData().getApplication().setApplicant1CannotUpload(YesOrNo.NO);

        var response = citizenAddPartnerContactDetails.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.Submitted);
    }

    @Test
    void shouldReturnErrorIfRespondentAddressAlreadyExists() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        caseDetails.setData(getCaseData(true));
        caseDetails.setState(State.AwaitingDocuments);

        var response = citizenAddPartnerContactDetails.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).contains(CANNOT_UPDATE_PARTNER_DETAILS_ERROR);
    }

    @Test
    void shouldApplyDataToRespondentAddress() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        caseDetails.setData(getCaseData(false));
        caseDetails.setState(State.AwaitingDocuments);

        caseDetails.getData().getApplication().setApplicant1CannotUpload(YesOrNo.YES);

        var response = citizenAddPartnerContactDetails.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getApplicant2().getAddress()).isEqualTo(getNewAddress());
        assertThat(response.getData().getApplicant2().getEmail()).isEqualTo("test");
    }

    private CaseData getCaseData(boolean addRespondentAddress) {
        var caseData = validCaseDataForIssueApplication();
        NoRespondentAddressJourneyOptions noRespAddressJourneyOptions = NoRespondentAddressJourneyOptions.builder()
            .noRespAddressAddress(getNewAddress())
            .noRespAddressEmail("test")
            .build();
        InterimApplicationOptions interimApplicationOptions =
            InterimApplicationOptions.builder().noRespAddressJourneyOptions(noRespAddressJourneyOptions).build();
        caseData.getApplicant1().setInterimApplicationOptions(interimApplicationOptions);

        if (!addRespondentAddress) {
            caseData.getApplicant2().setAddress(null);
        }
        return caseData;
    }

    AddressGlobalUK getNewAddress() {
        return AddressGlobalUK
            .builder()
            .addressLine1("newline1")
            .addressLine2("newline2")
            .postTown("town")
            .postCode("postcode")
            .build();
    }
}
