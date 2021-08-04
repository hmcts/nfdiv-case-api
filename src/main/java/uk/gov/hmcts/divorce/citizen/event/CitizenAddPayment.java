package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.IN_PROGRESS;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

@Component
@Slf4j
public class CitizenAddPayment implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_ADD_PAYMENT = "citizen-add-payment";

    @Autowired
    private ApplicationOutstandingActionNotification outstandingActionNotification;

    @Autowired
    private ApplicationSubmittedNotification notification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(CITIZEN_ADD_PAYMENT)
            .forState(AwaitingPayment)
            .name("Payment made")
            .description("Payment made")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN, CASEWORKER_COURTADMIN_RDU, CASEWORKER_COURTADMIN_CTSC)
            .grant(READ, CASEWORKER_SUPERUSER)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Add payment about to submit callback invoked");

        CaseData data = details.getData();

        log.info("Validating case data");
        List<String> submittedErrors = Submitted.validate(data);
        List<String> awaitingDocumentsErrors = AwaitingDocuments.validate(data);
        State state = details.getState();
        List<String> errors = Stream.concat(submittedErrors.stream(), awaitingDocumentsErrors.stream())
            .collect(Collectors.toList());
        PaymentStatus lastPaymentStatus = data.getApplication().getLastPaymentStatus();

        if (IN_PROGRESS.equals(lastPaymentStatus)) {
            log.info("Case {} payment in progress", details.getId());

            state = AwaitingPayment;
            errors.clear();
        } else if (!SUCCESS.equals(lastPaymentStatus)) {
            log.info("Case {} payment canceled", details.getId());

            state = Draft;
            errors.clear();
        } else if (submittedErrors.isEmpty()) {
            log.info("Case {} submitted", details.getId());
            data.getApplication().setDateSubmitted(LocalDateTime.now());

            notification.send(data, details.getId());
            state = Submitted;
            errors.clear();
        } else if (awaitingDocumentsErrors.isEmpty()) {
            log.info("Case {} awaiting documents", details.getId());
            data.getApplication().setDateSubmitted(LocalDateTime.now());

            notification.send(data, details.getId());
            outstandingActionNotification.send(data, details.getId());
            state = AwaitingDocuments;
            errors.clear();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(state)
            .errors(errors)
            .build();
    }
}

