package uk.gov.hmcts.divorce.citizen.event;

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
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import javax.servlet.http.HttpServletRequest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.citizen.event.CitizenUpdateContactDetails.CITIZEN_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CitizenUpdateContactDetailsTest {

    @InjectMocks
    private CitizenUpdateContactDetails citizenUpdateContactDetails;

    @Mock
    private IdamService idamService;

    @Mock
    private HttpServletRequest request;

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
        updatedData.getApplicant1().setHomeAddress(address);

        updatedCaseDetails.setData(updatedData);
        previousCaseDetails.setData(caseData);

        when(request.getHeader(AUTHORIZATION))
            .thenReturn("token");

        final var userDetails = UserDetails.builder()
            .email("test@test.com")
            .id("app1")
            .roles(Collections.singletonList(CITIZEN.getRole()))
            .build();

        when(idamService.retrieveUser(anyString()))
            .thenReturn(new User("token", userDetails));

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenUpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant1().getPhoneNumber()).isEqualTo("01234567890");
        assertThat(response.getData().getApplicant1().getHomeAddress()).isEqualTo(address);
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
        updatedData.getApplicant2().setHomeAddress(address);

        updatedCaseDetails.setData(updatedData);
        previousCaseDetails.setData(caseData);

        when(request.getHeader(AUTHORIZATION))
            .thenReturn("token");

        final var userDetails = UserDetails.builder()
            .email("test@test.com")
            .id("app1")
            .roles(Collections.singletonList(APPLICANT_2.getRole()))
            .build();

        when(idamService.retrieveUser(anyString()))
            .thenReturn(new User("token", userDetails));

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenUpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant2().getPhoneNumber()).isEqualTo("01234567890");
        assertThat(response.getData().getApplicant2().getHomeAddress()).isEqualTo(address);
    }
}
