package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.citizen.event.CitizenUpdateContactDetails.CITIZEN_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CitizenUpdateContactDetailsTest {

    @Mock
    private CcdAccessService ccdAccessService;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CitizenUpdateContactDetails citizenUpdateContactDetails;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenUpdateContactDetails.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_UPDATE_CONTACT_DETAILS);
    }

    @Test
    void shouldUpdateApplicant1AddressAndPhoneNumberFields() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final CaseData updatedData = CaseData.builder().build();
        updatedData.getApplicant1().setPhoneNumber("01234567890");
        AddressGlobalUK address = AddressGlobalUK.builder()
            .addressLine1("123 The Street")
            .postTown("The town")
            .county("County Durham")
            .country("England")
            .postCode("POSTCODE")
            .build();
        updatedData.getApplicant1().setAddress(address);
        updatedData.getApplicant1().setContactDetailsType(PUBLIC);
        updatedCaseDetails.setData(updatedData);
        updatedCaseDetails.setId(123456789L);
        previousCaseDetails.setData(caseData);
        when(request.getHeader(AUTHORIZATION)).thenReturn("token");
        when(ccdAccessService.isApplicant1("token", 123456789L)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenUpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant1().getPhoneNumber()).isEqualTo("01234567890");
        assertThat(response.getData().getApplicant1().getAddress()).isEqualTo(address);
        assertThat(response.getData().getApplicant1().getContactDetailsType()).isEqualTo(PUBLIC);
    }

    @Test
    void shouldUpdateApplicant2AddressAndPhoneNumberFields() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        final CaseData updatedData = CaseData.builder().build();
        updatedData.getApplicant2().setPhoneNumber("01234567890");
        AddressGlobalUK address = AddressGlobalUK.builder()
            .addressLine1("123 The Street")
            .postTown("The town")
            .county("County Durham")
            .country("England")
            .postCode("POSTCODE")
            .build();
        updatedData.getApplicant2().setAddress(address);
        updatedData.getApplicant2().setContactDetailsType(PUBLIC);
        updatedData.setCaseInvite(
            CaseInvite.builder()
                .applicant2UserId("app2")
                .build()
        );
        updatedCaseDetails.setData(updatedData);
        updatedCaseDetails.setId(123456789L);
        previousCaseDetails.setData(caseData);
        when(request.getHeader(AUTHORIZATION)).thenReturn("token");
        when(ccdAccessService.isApplicant1("token", 123456789L)).thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenUpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant2().getPhoneNumber()).isEqualTo("01234567890");
        assertThat(response.getData().getApplicant2().getAddress()).isEqualTo(address);
        assertThat(response.getData().getApplicant2().getContactDetailsType()).isEqualTo(PUBLIC);
    }
}
