package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.citizen.notification.SendCertificateOfServiceNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.citizen.event.CitizenEvidenceCertificateOfService.CITIZEN_EVIDENCE_CERTIFICATE_OF_SERVICE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
class CitizenEvidenceCertificateOfServiceTest {
    @Mock
    private SendCertificateOfServiceNotification sendCertificateOfServiceNotification;

    @InjectMocks
    private CitizenEvidenceCertificateOfService citizenEvidenceCertificateOfService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenEvidenceCertificateOfService.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_EVIDENCE_CERTIFICATE_OF_SERVICE);
    }

    @Test
    void givenCallbackIsInvokedThenSendEmail() {
        final var caseData = caseData();
        final var details = new CaseDetails<CaseData, State>();
        details.setData(caseData);
        details.setId(123456789L);

        citizenEvidenceCertificateOfService.submitted(details, details);

        verify(sendCertificateOfServiceNotification).notifyApplicant(caseData, 123456789L);
    }
}
