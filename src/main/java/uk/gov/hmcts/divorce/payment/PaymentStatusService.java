package uk.gov.hmcts.divorce.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Service
@Slf4j
public class PaymentStatusService {

    private static final String SUCCESS = "Success";

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    public void hasSuccessFulPayment(List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> casesInAwaitingPaymentState) {
        final List<CaseDetails<CaseData, State>> casesWithInProgressPayments = casesInAwaitingPaymentState
            .stream()
            .filter(cd -> cd.getData().containsKey("applicationPayments"))
            .map(cd -> caseDetailsConverter.convertToCaseDetailsFromReformModel(cd))
            .filter(this::hasInProgressPayment)
            .toList();

        log.info("PaymentStatusService: {} cases with payments In Progress",
            casesWithInProgressPayments.size());

        final List<Long> caseIds = casesWithInProgressPayments
            .parallelStream()
            .filter(this::hasSuccessfulPayment)
            .map(CaseDetails::getId)
            .toList();

        log.info("PaymentStatusService found with successful payments: " + caseIds);
    }

    private boolean hasInProgressPayment(uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails) {
        return Optional.ofNullable(caseDetails.getData().getApplication().getApplicationPayments())
            .orElse(emptyList())
            .stream()
            .anyMatch(ap -> ap.getValue().getStatus().equals(PaymentStatus.IN_PROGRESS));
    }

    private boolean hasSuccessfulPayment(uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails) {

        final List<ListValue<uk.gov.hmcts.divorce.divorcecase.model.Payment>> applicationPayments
            = caseDetails.getData().getApplication().getApplicationPayments();

        return Optional.ofNullable(applicationPayments)
            .orElse(emptyList())
            .stream()
            .filter(ap -> ap.getValue().getStatus().equals(PaymentStatus.IN_PROGRESS))
            .map(ap -> ap.getValue().getReference())
            .filter(Objects::nonNull)
            .map(this::paymentSuccessful)
            .findFirst()
            .orElse(false);
    }

    private boolean paymentSuccessful(String paymentReference) {
        final Payment payment = paymentClient.getPaymentByReference(
            idamService.retrieveSystemUpdateUserDetails().getAuthToken(),
            authTokenGenerator.generate(),
            paymentReference
        );

        return SUCCESS.equalsIgnoreCase(payment.getStatus());
    }
}
