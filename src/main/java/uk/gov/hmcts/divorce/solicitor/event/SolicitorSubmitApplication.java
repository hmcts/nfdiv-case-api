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
import uk.gov.hmcts.divorce.solicitor.event.page.HelpWithFees;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayAccount;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayment;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPaymentSummary;
import uk.gov.hmcts.divorce.solicitor.event.page.SolStatementOfTruth;
import uk.gov.hmcts.divorce.solicitor.event.page.SolSummary;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitApplicationService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.common.model.State.Draft;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

@Slf4j
@Component
public class SolicitorSubmitApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_SUBMIT = "solicitor-submit-application";

    @Autowired
    private SolicitorSubmitApplicationService solicitorSubmitApplicationService;

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

        log.info("Submit application about to start callback invoked");

        log.info("Retrieving order summary");
        final OrderSummary orderSummary = solicitorSubmitApplicationService.getOrderSummary();
        final CaseData caseData = details.getData();
        caseData.setApplicationFeeOrderSummary(orderSummary);

        log.info("Adding Applicant 1 solicitor case roles");
        ccdAccessService.addApplicant1SolicitorRole(
            httpServletRequest.getHeader(AUTHORIZATION),
            details.getId()
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit application about to submit callback invoked");

        final CaseData caseData = details.getData();
        final State currentState = details.getState();

        log.info("Setting dummy payment to mock payment process");
        if (caseData.getPayments() == null || caseData.getPayments().isEmpty()) {
            List<ListValue<Payment>> payments = new ArrayList<>();
            payments.add(new ListValue<>(null,
                solicitorSubmitApplicationService.getDummyPayment(caseData.getApplicationFeeOrderSummary())));
            caseData.setPayments(payments);
        } else {
            caseData.getPayments()
                .add(new ListValue<>(null,
                    solicitorSubmitApplicationService.getDummyPayment(caseData.getApplicationFeeOrderSummary())));
        }

        updateApplicant2DigitalDetails(caseData);

        if (!caseData.hasStatementOfTruth() && !caseData.hasSolSignStatementOfTruth()) {

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(currentState)
                .errors(singletonList("Statement of truth must be accepted by the person making the application"))
                .build();
        }

        return solicitorSubmitApplicationService.aboutToSubmit(caseData, details.getId(), httpServletRequest.getHeader(AUTHORIZATION));
    }

    private void updateApplicant2DigitalDetails(CaseData caseData) {
        if (caseData.hasDigitalDetailsForApp2Sol() && caseData.hasApplicant2OrgId()) {
            log.info("Applicant 2 solicitor is digital and applicant 2 org is populated");
            caseData.setApp2ContactMethodIsDigital(YES);
            caseData.setApplicant2SolicitorRepresented(YES);
        }
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final List<ListValue<Payment>> payments = caseData.getPayments() == null ? emptyList() : caseData.getPayments();
        final int feesPaid = payments.stream()
            .filter(payment -> payment.getValue().getPaymentStatus().equals(SUCCESS))
            .mapToInt(payment -> payment.getValue().getPaymentAmount())
            .sum();

        if (String.valueOf(feesPaid).equals(caseData.getApplicationFeeOrderSummary().getPaymentTotal())) {
            details.setState(Submitted);
        } else {
            details.setState(AwaitingPayment);
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder.event(SOLICITOR_SUBMIT)
            .forStates(Draft)
            .name("Case submission")
            .description("Agree Statement of Truth, Pay & Submit")
            .displayOrder(1)
            .showSummary()
            .endButtonLabel("Submit Application")
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
