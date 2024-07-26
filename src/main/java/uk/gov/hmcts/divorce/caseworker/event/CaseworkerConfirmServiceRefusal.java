package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.ServiceApplicationNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.divorcecase.model.State.ServiceAdminRefusal;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerConfirmServiceRefusal implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CONFIRM_SERVICE_REFUSAL = "caseworker-confirm-service-refusal";

    private final NotificationDispatcher notificationDispatcher;
    private final ServiceApplicationNotification serviceApplicationNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CONFIRM_SERVICE_REFUSAL)
            .forState(ServiceAdminRefusal)
            .name("CW confirm service refusal")
            .description("CW confirm service refusal")
            .submittedCallback(this::submitted)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE, SOLICITOR));
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker confirm service refusal Submitted callback invoked, Case Id: {}", details.getId());

        CaseData caseData = details.getData();

        //Set field temporarily to validate service application refused.
        caseData.getAlternativeService().setServiceApplicationGranted(YesOrNo.NO);

        notificationDispatcher.send(serviceApplicationNotification, caseData, details.getId());

        return SubmittedCallbackResponse.builder().build();
    }
}
