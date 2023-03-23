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
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplication;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.citizen.event.CitizenApplicant2UpdateContactDetails.CITIZEN_APPLICANT2_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
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
    private GenerateApplication generateApplication;

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
    void shouldUpdateApplicant2AddressAndRegenerateDivorceApplicationWithChangedAddressWhenContactIsNotPrivateAndAwaitingAos() {
        verifyAddressUpdate(AwaitingAos);
        verify(divorceApplicationRemover).apply(any());
        verify(generateApplication).apply(any());
    }

    @Test
    void shouldUpdateApplicant2AddressAndRegenerateDivorceApplicationWithChangedAddressWhenContactIsNotPrivateAndAosOverdue() {
        verifyAddressUpdate(AosOverdue);
        verify(divorceApplicationRemover).apply(any());
        verify(generateApplication).apply(any());
    }

    @Test
    void shouldUpdateApplicant2AddressAndRegenerateDivorceApplicationWithChangedAddressWhenContactIsNotPrivateAndAosDrafted() {
        verifyAddressUpdate(AosDrafted);
        verify(divorceApplicationRemover).apply(any());
        verify(generateApplication).apply(any());
    }

    @Test
    void shouldUpdateApplicant2AddressAndShouldNotRegenerateDivorceApplicationWithChangedAddressAfterSubmittingAOSWhenContactIsNotPrivate(
    ) {
        verifyAddressUpdate(Holding);
        verifyNoInteractions(divorceApplicationRemover);
        verifyNoInteractions(generateApplication);
    }

    @Test
    void shouldUpdateApplicant2PhoneNumberAndShouldNotRegenerateDivorceApplicationBeforeSubmittingAOSWhenContactIsNotPrivate() {
        verifyPhoneNumberUpdate(AwaitingAos);
        verifyNoInteractions(divorceApplicationRemover);
        verifyNoInteractions(generateApplication);
    }

    @Test
    void shouldUpdateApplicant2PhoneNumberAndShouldNotRegenerateDivorceApplicationAfterSubmittingAOSWhenContactIsNotPrivate() {
        verifyPhoneNumberUpdate(Holding);
        verifyNoInteractions(divorceApplicationRemover);
        verifyNoInteractions(generateApplication);
    }

    @Test
    void shouldRegenerateDivorceApplicationWhenContactPrivacyIsChangedBeforeSubmittingAOS() {
        verifyContactPrivacyUpdate(PRIVATE, PUBLIC, AwaitingAos);
        verify(divorceApplicationRemover).apply(any());
        verify(generateApplication).apply(any());
    }

    @Test
    void shouldNotRegenerateDivorceApplicationWhenContactPrivacyIsChangedAfterSubmittingAOS() {
        verifyContactPrivacyUpdate(PRIVATE, PUBLIC, Holding);
        verifyNoInteractions(divorceApplicationRemover);
        verifyNoInteractions(generateApplication);
    }

    @Test
    void shouldNotRegenerateDivorceApplicationWhenContactPrivacyIsNotChanged() {
        verifyContactPrivacyUpdate(PUBLIC, PUBLIC, AwaitingAos);
        verifyNoInteractions(divorceApplicationRemover);
        verifyNoInteractions(generateApplication);
    }

    private void verifyAddressUpdate(State state) {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();

        setupData(updatedCaseDetails, previousCaseDetails, state);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenApplicant2UpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant2().getAddress()).isEqualTo(ADDRESS2);
        assertThat(response.getData().getApplicant2().getContactDetailsType()).isEqualTo(PUBLIC);
    }

    private void verifyPhoneNumberUpdate(State state) {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();

        setupData(updatedCaseDetails, previousCaseDetails, state);

        previousCaseDetails.getData().getApplicant2().setAddress(ADDRESS1);
        updatedCaseDetails.getData().getApplicant2().setAddress(ADDRESS1);
        previousCaseDetails.getData().getApplicant2().setPhoneNumber("1122334455");
        updatedCaseDetails.getData().getApplicant2().setPhoneNumber("01234567890");

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenApplicant2UpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant2().getPhoneNumber()).isEqualTo("01234567890");
        assertThat(response.getData().getApplicant2().getContactDetailsType()).isEqualTo(PUBLIC);
    }

    private void verifyContactPrivacyUpdate(ContactDetailsType oldContactDetailsType, ContactDetailsType newContactDetailsType,
                                            State state) {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();

        setupData(updatedCaseDetails, previousCaseDetails, state);

        previousCaseDetails.getData().getApplicant2().setAddress(ADDRESS1);
        updatedCaseDetails.getData().getApplicant2().setAddress(ADDRESS1);
        previousCaseDetails.getData().getApplicant2().setPhoneNumber("1122334455");
        updatedCaseDetails.getData().getApplicant2().setPhoneNumber("1122334455");

        previousCaseDetails.getData().getApplicant2().setContactDetailsType(oldContactDetailsType);
        updatedCaseDetails.getData().getApplicant2().setContactDetailsType(newContactDetailsType);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            citizenApplicant2UpdateContactDetails.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getData().getApplicant2().getContactDetailsType()).isEqualTo(newContactDetailsType);
    }

    private void setupData(final CaseDetails<CaseData, State> updatedCaseDetails,
                           final CaseDetails<CaseData, State> previousCaseDetails,
                           final State state) {
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
        updatedCaseDetails.setState(state);
        previousCaseDetails.setData(caseData);
        previousCaseDetails.setState(state);
        when(request.getHeader(AUTHORIZATION)).thenReturn("token");
        when(ccdAccessService.isApplicant1("token", 123456789L)).thenReturn(false);
    }
}
