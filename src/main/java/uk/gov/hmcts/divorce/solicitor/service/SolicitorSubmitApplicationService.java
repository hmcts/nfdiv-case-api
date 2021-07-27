package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.CaseInfo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.solicitor.service.task.MiniApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.task.SolicitorSubmitNotification;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEES_HELP_WITH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

@Service
@Slf4j
public class SolicitorSubmitApplicationService {

    @Autowired
    private MiniApplicationRemover miniApplicationRemover;

    @Autowired
    private SolicitorSubmitNotification solicitorSubmitNotification;

    @Autowired
    private Clock clock;

    public CaseInfo aboutToSubmit(final CaseDetails<CaseData, State> caseDetails) {

        final List<String> submittedErrors = new ArrayList<>();

        final CaseTask feesTask = cd -> {
            final CaseData caseData = cd.getData();

            if (FEES_HELP_WITH.equals(caseData.getApplication().getSolPaymentHowToPay())) {
                cd.setState(AwaitingHWFDecision);
            } else {
                submittedErrors.addAll(Submitted.validate(caseData));

                if (submittedErrors.isEmpty()) {
                    caseData.getApplication().setDateSubmitted(LocalDateTime.now(clock));
                    cd.setState(Submitted);
                } else {
                    cd.setState(AwaitingPayment);
                }
            }
            return caseDetails;
        };

        final CaseDetails<CaseData, State> updatedCaseDetails = caseTasks(
            feesTask,
            miniApplicationRemover,
            solicitorSubmitNotification
        ).run(caseDetails);

        return CaseInfo.builder()
            .caseData(updatedCaseDetails.getData())
            .state(updatedCaseDetails.getState())
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
