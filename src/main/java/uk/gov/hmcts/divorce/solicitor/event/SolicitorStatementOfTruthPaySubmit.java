package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;
import uk.gov.hmcts.divorce.solicitor.event.page.HelpWithFees;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayAccount;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayment;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPaymentSummary;
import uk.gov.hmcts.divorce.solicitor.event.page.SolStatementOfTruth;
import uk.gov.hmcts.divorce.solicitor.event.page.SolSummary;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitPetitionService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.divorce.common.model.State.SolicitorAwaitingPaymentConfirmation;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;

@Slf4j
@Component
public class SolicitorStatementOfTruthPaySubmit implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT = "solicitor-statement-of-truth-pay-submit";

    @Autowired
    private SolicitorSubmitPetitionService solicitorSubmitPetitionService;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    private final List<CcdPageConfiguration> pages = asList(
        new SolStatementOfTruth(),
        new SolPayment(),
        new HelpWithFees(),
        new SolPayAccount(),
        new SolPaymentSummary(),
        new SolSummary());

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Submit petition about to start callback invoked");

        log.info("Retrieving order summary");
        final OrderSummary orderSummary = solicitorSubmitPetitionService.getOrderSummary();
        final CaseData caseData = details.getData();
        caseData.setSolApplicationFeeOrderSummary(orderSummary);

        log.info("Adding Petitioner solicitor case roles");
        ccdAccessService.addPetitionerSolicitorRole(
            httpServletRequest.getHeader(AUTHORIZATION),
            details.getId()
        );

        log.info("Setting dummy payment to mock payment process");
        if (caseData.getPayments() == null || caseData.getPayments().isEmpty()) {
            List<ListValue<Payment>> payments = new ArrayList<>();
            payments.add(
                ListValue
                    .<Payment>builder()
                    .value(solicitorSubmitPetitionService.getDummyPayment(orderSummary))
                    .build());
            caseData.setPayments(payments);
        } else {
            caseData.getPayments().add(
                ListValue
                    .<Payment>builder()
                    .value(solicitorSubmitPetitionService.getDummyPayment(orderSummary))
                    .build());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit petition about to submit callback invoked");

        final CaseData caseData = details.getData();

        if (!caseData.hasStatementOfTruth() || !caseData.hasSolSignStatementOfTruth()) {

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .state(details.getState())
                .errors(asList("Statement of truth for solicitor and applicant 1 needs to be accepted"))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(SolicitorAwaitingPaymentConfirmation)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final PaymentStatus paymentStatus = caseData.getPayments().get(caseData.getPayments().size() - 1).getValue().getPaymentStatus();

        if (paymentStatus.equals(PaymentStatus.SUCCESS)) {
            details.setState(Submitted);
        } else {
            details.setState(SolicitorAwaitingPaymentConfirmation);
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder.event(SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)
            .forStates(SOTAgreementPayAndSubmitRequired)
            .name("Case submission")
            .description("Agree Statement of Truth, Pay & Submit")
            .displayOrder(1)
            .showSummary()
            .endButtonLabel("Submit Petition")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_DIVORCE_SOLICITOR)
            .grant(READ,
                CASEWORKER_DIVORCE_COURTADMIN_BETA,
                CASEWORKER_DIVORCE_COURTADMIN,
                CASEWORKER_DIVORCE_SUPERUSER,
                CASEWORKER_DIVORCE_COURTADMIN_LA)
            .submittedCallback(this::submitted));
    }
}
