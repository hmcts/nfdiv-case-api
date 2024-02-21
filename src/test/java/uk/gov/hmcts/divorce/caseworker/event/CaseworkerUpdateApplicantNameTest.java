package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.event.page.UpdateApplicantName;
import uk.gov.hmcts.divorce.caseworker.service.notification.Applicant1NameChangeNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.*;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateApplicantName.CASEWORKER_UPDATE_APPLICANT_NAME;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseworkerUpdateApplicantNameTest {
    @InjectMocks
    private UpdateApplicantName updateApplicantName;
    @Mock
    private NotificationDispatcher notificationDispatcher;
    @Mock
    private Applicant1NameChangeNotification applicant1NameChangeNotification;
    @InjectMocks
    private CaseworkerUpdateApplicantName caseworkerUpdateApplicantName;
    private final CaseDetails<CaseData, State> validCaseData = new CaseDetails<>();

    @BeforeEach
    void setUp() {
        validCaseData.setData(CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName("Test")
                .lastName("User")
                .build())
            .build());
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();
        caseworkerUpdateApplicantName.configure(configBuilder);

        verify(updateApplicantName).addTo(any(PageBuilder.class));
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPDATE_APPLICANT_NAME);
    }

    @Test
    void aboutToStartCallbackTestForValidApplicantName() {
        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicantName.aboutToStart(validCaseData);
        assertNull(response.getErrors());
    }

    @Test
    void aboutToStartCallbackFailWhenApplicantNameDoesNotExist() {
        final CaseDetails<CaseData, State> caseData = new CaseDetails<>();
        caseData.setData(CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName(null)
                .lastName(null)
                .build())
            .build());
        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicantName.aboutToStart(caseData);
        assertNotNull(response.getErrors());
        assertEquals(1, response.getErrors().size());
        assertThat(response.getErrors().get(0)).isEqualTo("Applicant Name does not exist");
    }

    @Test
    void aboutToStartCallbackTestForInvalidApplicantName() {
        final CaseDetails<CaseData, State> invalidCaseData = new CaseDetails<>();
        invalidCaseData.setData(CaseData.builder()
        .applicant1(Applicant.builder()
            .firstName("abc")
            .lastName("x")
            .build())
        .build());

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicantName.aboutToStart(invalidCaseData);
        assertNull(response.getErrors());
    }

    @Test
    void shouldSendApplicant1NotificationWhenNameChangeSucessful() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        caseworkerUpdateApplicantName.submitted(details, details);
        verify(notificationDispatcher).send(applicant1NameChangeNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void validateNameTestSuccess() {
        List<String> expErrors = new ArrayList<>();
        System.out.println("expected error list size " + expErrors.size());
        assertThat(caseworkerUpdateApplicantName.validateNameExists(validCaseData).size()).isEqualTo(0);
    }

    @Test
    void validateNameTestFail() {
        final CaseDetails<CaseData, State> caseData = new CaseDetails<>();
        caseData.setData(CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName(null)
                .lastName(null)
                .build())
            .build());
        caseData.setState(Submitted);
        List<String> expErrors = new ArrayList<>();
        expErrors.add("Applicant Name does not exist");
        System.out.println("expected error list size " + expErrors.size());
        assertThat(caseworkerUpdateApplicantName.validateNameExists(caseData)).isEqualTo(expErrors);
        assertThat(caseworkerUpdateApplicantName.validateNameExists(caseData).size()).isEqualTo(1);
    }
}
