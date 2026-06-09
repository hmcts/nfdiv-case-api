package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.UpdateApplicant1NameNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateApplicant1Name.CASEWORKER_UPDATE_APP1_NAME;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseworkerUpdateApplicant1NameTest {

    @InjectMocks
    private CaseworkerUpdateApplicant1Name caseworkerUpdateApplicant1Name;
    @Mock
    private UpdateApplicant1NameNotification updateApplicant1NameNotification;
    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUpdateApplicant1Name.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPDATE_APP1_NAME);
    }

    @ParameterizedTest
    @MethodSource("nullOrEmptyNamePairs")
    void shouldReturnErrorInAboutToStartCallbackWhenApplicantFirstAndLastNameIsNullOrEmpty(String firstName,
                                                                                           String lastName) {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant1Name.aboutToStart(details);
        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors())
            .containsExactlyInAnyOrder(
                "Applicant first name is not provided to amend their first name",
                "Applicant last name is not provided  to amend their last name"
            );
    }

    private static Stream<Arguments> nullOrEmptyNamePairs() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of("", ""),
            Arguments.of(null, ""),
            Arguments.of("", null)
        );
    }

    @Test
    void shouldNotReturnErrorInAboutToStartCallbackWhenApplicantFirstAndLastNameIsPresent() {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName("John")
                .lastName("Smith")
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant1Name.aboutToStart(details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorInMidLevelCallbackWhenApplicantFirstAndLastNameIsLessThanThreeCharacters() {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName("Jo")
                .lastName("Sm")
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant1Name.midEvent(details, details);
        assertThat(response.getErrors()).hasSize(2);
    }

    @Test
    void shouldNotReturnErrorInMidEventWhenApplicantFirstAndLastNameIsGreaterThanThreeCharacters() {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName("John")
                .lastName("Smith")
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant1Name.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldSendNotificationInAboutToSubmitWhenApplicantNameChanges() {
        final CaseData beforeCaseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName("John")
                .lastName("Smith")
                .build())
            .build();
        final CaseData updatedCaseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName("Johnny")
                .lastName("Smith")
                .build())
            .build();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setId(TEST_CASE_ID);
        beforeDetails.setData(beforeCaseData);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(updatedCaseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateApplicant1Name.aboutToSubmit(details, beforeDetails);
        verify(notificationDispatcher).send(updateApplicant1NameNotification, updatedCaseData, TEST_CASE_ID);
        assertThat(response.getData()).isSameAs(updatedCaseData);
    }

    @Test
    void shouldNotSendNotificationInAboutToSubmitWhenApplicantNameIsUnchanged() {
        final CaseData beforeCaseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName("John")
                .lastName("Smith")
                .build())
            .build();

        final CaseData updatedCaseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName("John")
                .lastName("Smith")
                .build())
            .build();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setId(TEST_CASE_ID);
        beforeDetails.setData(beforeCaseData);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(updatedCaseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUpdateApplicant1Name.aboutToSubmit(details, beforeDetails);
        verify(notificationDispatcher, never()).send(updateApplicant1NameNotification, updatedCaseData, TEST_CASE_ID);

        assertThat(response.getErrors()).isNull();
    }
}
