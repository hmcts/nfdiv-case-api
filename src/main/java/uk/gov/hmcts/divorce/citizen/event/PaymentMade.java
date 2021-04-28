package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.common.model.State.Draft;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;

@Component
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
            .submittedCallback(this::submitted);
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {
        outstandingActionNotification.send(details.getData(), details.getId());
        notification.send(details.getData(), details.getId());

        return SubmittedCallbackResponse.builder().build();
    }
}

