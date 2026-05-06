package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.citizen.notification.SendCertificateOfServiceNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CitizenEvidenceCertificateOfService implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_EVIDENCE_CERTIFICATE_OF_SERVICE = "citizen-evidence-certificate-of-service";

    private final SendCertificateOfServiceNotification sendCertificateOfServiceNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_EVIDENCE_CERTIFICATE_OF_SERVICE)
            .forAllStates()
            .showCondition(NEVER_SHOW)
            .name("Send certificate of service")
            .description("Send certificate of service")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .submittedCallback(this::submitted);
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for case id: {}", CITIZEN_EVIDENCE_CERTIFICATE_OF_SERVICE, details.getId());

        sendCertificateOfServiceNotification.notifyApplicant(
            details.getData(),
            details.getId()
        );

        return SubmittedCallbackResponse.builder().build();
    }
}
