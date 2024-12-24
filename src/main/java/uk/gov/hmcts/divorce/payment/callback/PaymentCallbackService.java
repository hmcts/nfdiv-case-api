package uk.gov.hmcts.divorce.payment.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static uk.gov.hmcts.divorce.citizen.event.CitizenPaymentMade.CITIZEN_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.citizen.event.RespondentFinalOrderPaymentMade.RESPONDENT_FINAL_ORDER_PAYMENT_MADE;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackService {

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final CoreCaseDataApi coreCaseDataApi;

    private final UpdateSuccessfulPaymentStatus updateSuccessfulPaymentStatus;

    public void handleCallback(PaymentCallbackDto paymentCallback) {
        User systemUpdateUser = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        if (!PaymentStatus.SUCCESS.getLabel().equals(paymentCallback.getStatus())) {
            log.info("Payment unsuccessful for case: {}, not processing callback", paymentCallback.getCcdCaseNumber());
        }

        String caseReference = paymentCallback.getCcdCaseNumber();
        CaseDetails details = coreCaseDataApi.getCase(systemUpdateUser.getAuthToken(), serviceAuthorization, caseReference);
        State state = State.valueOf(details.getState());

        String paymentMadeEvent = paymentMadeEvent(state);

        if (paymentMadeEvent != null) {
            ccdUpdateService.submitEventWithRetry(
                caseReference,
                paymentMadeEvent(state),
                updateSuccessfulPaymentStatus,
                systemUpdateUser,
                serviceAuthorization
            );
        } else {
            log.info("Case not in awaiting payment state: {}, not processing callback", paymentCallback.getCcdCaseNumber());
        }
    }

    private String paymentMadeEvent(State state) {
        return switch (state) {
            case AwaitingPayment -> CITIZEN_PAYMENT_MADE;
            case AwaitingFinalOrderPayment -> RESPONDENT_FINAL_ORDER_PAYMENT_MADE;
            default -> null;
        };
    }
}
