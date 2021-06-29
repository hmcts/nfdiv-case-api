package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.CaseInfo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.updater.SolicitorSubmitNotification;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static uk.gov.hmcts.divorce.common.model.SolToPay.FEES_HELP_WITH;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

@Service
@Slf4j
public class SolicitorSubmitApplicationService {

    @Autowired
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @Autowired
    private MiniApplicationRemover miniApplicationRemover;

    @Autowired
    private SolicitorSubmitNotification solicitorSubmitNotification;

    @Autowired
    private Clock clock;

    public CaseInfo aboutToSubmit(
        final CaseData caseData,
        final Long caseId,
        final String userAuth
    ) {

        State state = AwaitingPayment;
        List<String> submittedErrors = emptyList();

        if (FEES_HELP_WITH.equals(caseData.getApplication().getSolPaymentHowToPay())) {
            state = AwaitingHWFDecision;
        } else {
            submittedErrors = Submitted.validate(caseData);

            if (submittedErrors.isEmpty()) {
                caseData.getApplication().setDateSubmitted(LocalDateTime.now(clock));
                state = Submitted;
            }
        }

        final var caseDataUpdaters = asList(
            miniApplicationRemover,
            solicitorSubmitNotification
        );

        final var caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(caseId)
            .userAuthToken(userAuth)
            .build();

        final var updatedCaseData = caseDataUpdaterChainFactory
            .createWith(caseDataUpdaters)
            .processNext(caseDataContext)
            .getCaseData();

        return CaseInfo.builder()
            .caseData(updatedCaseData)
            .state(state)
            .errors(submittedErrors)
            .build();
    }

    public Payment getDummyPayment(final OrderSummary orderSummary) {
        return Payment
            .builder()
            .paymentAmount(Integer.parseInt(orderSummary.getPaymentTotal()))
            .paymentChannel("online")
            .paymentFeeId("FEE0001")
            .paymentReference(orderSummary.getPaymentReference())
            .paymentSiteId("AA04")
            .paymentStatus(SUCCESS)
            .paymentTransactionId("ge7po9h5bhbtbd466424src9tk")
            .build();
    }
}
