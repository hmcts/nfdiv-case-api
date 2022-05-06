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
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateDivorceApplication;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.citizen.event.CitizenApplicant2UpdateContactDetails.CITIZEN_APPLICANT2_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CitizenApplicant2UpdateContactDetailsTest {

    private static final AddressGlobalUK ADDRESS1 = AddressGlobalUK.builder()
        .addressLine1("100 The Street")
        .postTown("The town")
        .county("County Durham")
        .country("England")
        .postCode("XXXXXX")
        .build();

    private static final AddressGlobalUK ADDRESS2 = AddressGlobalUK.builder()
        .addressLine1("123 The Street")
        .postTown("The town")
        .county("County Durham")
        .country("England")
        .postCode("POSTCODE")
        .build();

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private DivorceApplicationRemover divorceApplicationRemover;

    @Mock
    private GenerateDivorceApplication generateDivorceApplication;

    @InjectMocks
    private CitizenApplicant2UpdateContactDetails citizenApplicant2UpdateContactDetails;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenApplicant2UpdateContactDetails.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_APPLICANT2_UPDATE_CONTACT_DETAILS);
    }

    @Test
    void shouldUpdateApplicant2AddressAndRegenerateDivorceApplicationWithChangedAddressBeforeSubmittingAOSWhenContactIsNotPrivate() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant2().setAddress(ADDRESS1);

        final CaseData updatedData = CaseData.builder().build();
        updatedData.getApplicant2().setAddress(ADDRESS2);
        updatedData.getApplicant2().setContactDetailsType(PUBLIC);
        updatedData.setCaseInvite(
            CaseInvite.builder()
                .applicant2UserId("app2")
                .build()
        );
        updatedCaseDetails.setData(updatedData);
        updatedCaseDetails.setId(123456789L);
        updatedCaseDetails.setState(State.AwaitingAos);
        previousCaseDetails.setData(caseData);
        previousCaseDetails.setState(State.AwaitingAos);
        when(request.getHeader(AUTHORIZATION)).thenReturn("token");
        when(ccdAccessService.isApplicant1("token", 123456789L)).thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenApplicant2UpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant2().getAddress()).isEqualTo(ADDRESS2);
        assertThat(response.getData().getApplicant2().getContactDetailsType()).isEqualTo(PUBLIC);

        verify(divorceApplicationRemover).apply(any());
        verify(generateDivorceApplication).apply(any());
    }

    @Test
    void shouldUpdateApplicant2PhoneNumberAndShouldNotRegenerateDivorceApplicationBeforeSubmittingAOSWhenContactIsNotPrivate() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant2().setPhoneNumber("1122334455");

        final CaseData updatedData = CaseData.builder().build();
        updatedData.getApplicant2().setPhoneNumber("01234567890");
        updatedData.getApplicant2().setContactDetailsType(PUBLIC);
        updatedData.setCaseInvite(
            CaseInvite.builder()
                .applicant2UserId("app2")
                .build()
        );
        updatedCaseDetails.setData(updatedData);
        updatedCaseDetails.setId(123456789L);
        updatedCaseDetails.setState(State.AwaitingAos);
        previousCaseDetails.setData(caseData);
        previousCaseDetails.setState(State.AwaitingAos);
        when(request.getHeader(AUTHORIZATION)).thenReturn("token");
        when(ccdAccessService.isApplicant1("token", 123456789L)).thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenApplicant2UpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant2().getPhoneNumber()).isEqualTo("01234567890");
        assertThat(response.getData().getApplicant2().getContactDetailsType()).isEqualTo(PUBLIC);

        verifyNoInteractions(divorceApplicationRemover);
        verifyNoInteractions(generateDivorceApplication);
    }

    @Test
    void shouldUpdateApplicant2AddressAndShouldNotRegenerateDivorceApplicationWithChangedAddressAfterSubmittingAOSWhenContactIsNotPrivate(
    ) {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant2().setAddress(ADDRESS1);

        final CaseData updatedData = CaseData.builder().build();
        updatedData.getApplicant2().setAddress(ADDRESS2);
        updatedData.getApplicant2().setContactDetailsType(PUBLIC);
        updatedData.setCaseInvite(
            CaseInvite.builder()
                .applicant2UserId("app2")
                .build()
        );
        updatedCaseDetails.setData(updatedData);
        updatedCaseDetails.setId(123456789L);
        updatedCaseDetails.setState(State.Holding);
        previousCaseDetails.setData(caseData);
        previousCaseDetails.setState(State.Holding);
        when(request.getHeader(AUTHORIZATION)).thenReturn("token");
        when(ccdAccessService.isApplicant1("token", 123456789L)).thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenApplicant2UpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant2().getAddress()).isEqualTo(ADDRESS2);
        assertThat(response.getData().getApplicant2().getContactDetailsType()).isEqualTo(PUBLIC);

        verifyNoInteractions(divorceApplicationRemover);
        verifyNoInteractions(generateDivorceApplication);
    }

    @Test
    void shouldUpdateApplicant2PhoneNumberAndShouldNotRegenerateDivorceApplicationAfterSubmittingAOSWhenContactIsNotPrivate() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant2().setPhoneNumber("1122334455");

        final CaseData updatedData = CaseData.builder().build();
        updatedData.getApplicant2().setPhoneNumber("01234567890");
        updatedData.getApplicant2().setContactDetailsType(PUBLIC);
        updatedData.setCaseInvite(
            CaseInvite.builder()
                .applicant2UserId("app2")
                .build()
        );
        updatedCaseDetails.setData(updatedData);
        updatedCaseDetails.setId(123456789L);
        updatedCaseDetails.setState(State.Holding);
        previousCaseDetails.setData(caseData);
        previousCaseDetails.setState(State.Holding);
        when(request.getHeader(AUTHORIZATION)).thenReturn("token");
        when(ccdAccessService.isApplicant1("token", 123456789L)).thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenApplicant2UpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant2().getPhoneNumber()).isEqualTo("01234567890");
        assertThat(response.getData().getApplicant2().getContactDetailsType()).isEqualTo(PUBLIC);

        verifyNoInteractions(divorceApplicationRemover);
        verifyNoInteractions(generateDivorceApplication);
    }

    @Test
    void shouldRegenerateDivorceApplicationWhenContactPrivacyIsChangedBeforeSubmittingAOS() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant2().setAddress(ADDRESS1);
        caseData.getApplicant2().setContactDetailsType(PUBLIC);

        final CaseData updatedData = CaseData.builder().build();
        updatedData.getApplicant2().setAddress(ADDRESS2);
        updatedData.getApplicant2().setContactDetailsType(PRIVATE);
        updatedData.setCaseInvite(
            CaseInvite.builder()
                .applicant2UserId("app2")
                .build()
        );
        updatedCaseDetails.setData(updatedData);
        updatedCaseDetails.setId(123456789L);
        updatedCaseDetails.setState(State.AwaitingAos);
        previousCaseDetails.setData(caseData);
        previousCaseDetails.setState(State.AwaitingAos);
        when(request.getHeader(AUTHORIZATION)).thenReturn("token");
        when(ccdAccessService.isApplicant1("token", 123456789L)).thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenApplicant2UpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant2().getAddress()).isEqualTo(ADDRESS2);
        assertThat(response.getData().getApplicant2().getContactDetailsType()).isEqualTo(PRIVATE);

        verify(divorceApplicationRemover).apply(any());
        verify(generateDivorceApplication).apply(any());
    }

    @Test
    void shouldNotRegenerateDivorceApplicationWhenContactPrivacyIsNotChanged() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant2().setAddress(ADDRESS1);
        caseData.getApplicant2().setContactDetailsType(PRIVATE);

        final CaseData updatedData = CaseData.builder().build();
        updatedData.getApplicant2().setAddress(ADDRESS1);
        updatedData.getApplicant2().setContactDetailsType(PRIVATE);
        updatedData.setCaseInvite(
            CaseInvite.builder()
                .applicant2UserId("app2")
                .build()
        );
        updatedCaseDetails.setData(updatedData);
        updatedCaseDetails.setId(123456789L);
        updatedCaseDetails.setState(State.AwaitingAos);
        previousCaseDetails.setData(caseData);
        previousCaseDetails.setState(State.AwaitingAos);
        when(request.getHeader(AUTHORIZATION)).thenReturn("token");
        when(ccdAccessService.isApplicant1("token", 123456789L)).thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenApplicant2UpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant2().getAddress()).isEqualTo(ADDRESS1);
        assertThat(response.getData().getApplicant2().getContactDetailsType()).isEqualTo(PRIVATE);

        verifyNoInteractions(divorceApplicationRemover);
        verifyNoInteractions(generateDivorceApplication);
    }
}
