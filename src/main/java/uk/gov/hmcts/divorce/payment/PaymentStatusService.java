package uk.gov.hmcts.divorce.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Service
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

    public boolean hasSuccessFulPayment(CaseDetails cd) {
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails =
            caseDetailsConverter.convertToCaseDetailsFromReformModel(cd);

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

        return SUCCESS.equals(payment.getStatus());
    }
}
