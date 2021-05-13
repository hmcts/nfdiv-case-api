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
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.divorce.common.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.common.model.State.Draft;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class PaymentMade implements CCDConfig<CaseData, State, UserRole> {

    public static final String PAYMENT_MADE = "payment-made";

    @Autowired
    private ApplicationOutstandingActionNotification outstandingActionNotification;

    @Autowired
    private ApplicationSubmittedNotification notification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(PAYMENT_MADE)
            .initialState(Draft)
            .name("Payment made")
            .description("Payment made")
            .grant(CREATE_READ_UPDATE, CITIZEN, CASEWORKER_DIVORCE_COURTADMIN, CASEWORKER_DIVORCE_COURTADMIN_BETA)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();

        List<String> submittedErrors = Submitted.validate(data);
        List<String> awaitingDocumentsErrors = AwaitingDocuments.validate(data);
        State state = details.getState();
        List<String> errors = Stream.concat(submittedErrors.stream(), awaitingDocumentsErrors.stream())
            .collect(Collectors.toList());

        if (submittedErrors.isEmpty()) {
            log.info("Case {} submitted", details.getId());
            data.setDateSubmitted(LocalDateTime.now());

            notification.send(data, details.getId());
            state = Submitted;
            errors.clear();
        } else if (awaitingDocumentsErrors.isEmpty()) {
            log.info("Case {} awaiting documents", details.getId());
            data.setDateSubmitted(LocalDateTime.now());

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

