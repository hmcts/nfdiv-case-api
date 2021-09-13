package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.service.SubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateReadyForPayment;

@Slf4j
@Component
public class CitizenSubmitApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_SUBMIT = "citizen-submit-application";

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SubmissionService submissionService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_SUBMIT)
            .forStates(Draft, AwaitingPayment, Applicant2Approved)
            .name("Applicant Statement of Truth")
            .description("The applicant confirms SOT")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .grant(READ, SUPER_USER)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Submit application about to submit callback invoked");

        CaseData data = details.getData();
        State state = details.getState();

        log.info("Validating case data");
        final List<String> validationErrors = validateReadyForPayment(data);

        if (!validationErrors.isEmpty()) {
            log.info("Validation errors: ");
            for (String error : validationErrors) {
                log.info(error);
            }

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(validationErrors)
                .state(state)
                .build();
        }

        Application application = data.getApplication();

        if (application.isHelpWithFeesApplication()) {
            var submittedDetails = submissionService.submitApplication(details);
            data = submittedDetails.getData();
            state = submittedDetails.getState();
        } else {
            OrderSummary orderSummary = paymentService.getOrderSummary();
            application.setApplicationFeeOrderSummary(orderSummary);

            state = AwaitingPayment;
        }

        data.getLabelContent().setApplicationType(data.getApplicationType());
        data.getLabelContent().setUnionType(data.getDivorceOrDissolution());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(state)
            .build();
    }

}

