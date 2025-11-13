package uk.gov.hmcts.divorce.citizen.event;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.helper.GeneralReferralHelper;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.InterimApplicationSubmissionService;
import uk.gov.hmcts.divorce.common.service.PaymentValidatorService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralApplicationPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CitizenGeneralApplicationPaymentMade implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_GENERAL_APPLICATION_PAYMENT = "citizen-general-app-payment";

    private final Clock clock;

    private final PaymentValidatorService paymentValidatorService;

    private final InterimApplicationSubmissionService interimApplicationSubmissionService;

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest request;

    private final GeneralReferralHelper generalReferralHelper;

    private static final String GENERAL_APPLICATION_NOT_FOUND = "No general applications are awaiting payment";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CITIZEN_GENERAL_APPLICATION_PAYMENT)
            .forStates(AwaitingGeneralApplicationPayment)
            .name("General application payment")
            .description("General application payment made")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .showCondition(NEVER_SHOW)
            .grant(CREATE_READ_UPDATE, CREATOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        long caseId = details.getId();
        CaseData data = details.getData();
        log.info("{} About to Submit callback invoked for Case Id: {}", CITIZEN_GENERAL_APPLICATION_PAYMENT, caseId);

        boolean isApplicant1 = isApplicant1(caseId);
        Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        Optional<GeneralApplication> generalAppOptional = findActiveGeneralApplication(data, applicant);

        if (generalAppOptional.isEmpty()) {
            log.info("Failed to find active general application for payment, party: {}, case id: {}",
                isApplicant1 ? "Applicant 1" : "Respondent/Applicant2", caseId);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(GENERAL_APPLICATION_NOT_FOUND))
                .build();
        }

        GeneralApplication generalApplication = generalAppOptional.get();
        List<ListValue<Payment>> payments = applicant.getGeneralAppPayments();

        List<String> validationErrors = paymentValidatorService.validatePayments(payments, caseId);
        if (CollectionUtils.isNotEmpty(validationErrors)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .errors(validationErrors)
                    .build();
        }

        String paymentReference = paymentValidatorService.getLastPayment(payments).getReference();
        generalApplication.recordPayment(paymentReference, LocalDate.now(clock));
        applicant.setActiveGeneralApplication(null);

        if (hasGeneralReferralInProgress(data.getGeneralReferral())) {
            details.setState(GeneralApplicationReceived);
        } else {
            GeneralReferral automaticReferral = generalReferralHelper.buildGeneralReferral(generalApplication);
            data.setGeneralReferral(automaticReferral);

            details.setState(AwaitingGeneralConsideration);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", CITIZEN_GENERAL_APPLICATION_PAYMENT, details.getId());

        CaseData beforeData = beforeDetails.getData();
        CaseData data = details.getData();

        boolean isApplicant1 = isApplicant1(details.getId());
        Applicant beforeApplicant = isApplicant1 ? beforeData.getApplicant1() : beforeData.getApplicant2();
        Optional<GeneralApplication> generalAppOptional = findActiveGeneralApplication(data, beforeApplicant);

        generalAppOptional.ifPresent(generalApplication ->
            interimApplicationSubmissionService.sendGeneralApplicationNotifications(
                details.getId(), generalAppOptional.get(), data
            ));

        return SubmittedCallbackResponse.builder().build();
    }

    private boolean isApplicant1(Long caseId) {
        return ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), caseId);
    }

    private Optional<GeneralApplication> findActiveGeneralApplication(CaseData caseData, Applicant applicant) {
        String serviceRequest = applicant.getGeneralAppServiceRequest();

        if (CollectionUtils.isEmpty(caseData.getGeneralApplications()) || StringUtils.isBlank(serviceRequest)) {
            return Optional.empty();
        }

        return caseData.getGeneralApplications().stream()
            .map(ListValue::getValue)
            .filter(Objects::nonNull)
            .filter(application -> isActiveGeneralApplication(application, serviceRequest))
            .findFirst();
    }

    private boolean isActiveGeneralApplication(GeneralApplication generalApplication, String serviceRequest) {
        FeeDetails applicationFee = generalApplication.getGeneralApplicationFee();

        return applicationFee != null && applicationFee.getServiceRequestReference() != null
            && serviceRequest.equals(applicationFee.getServiceRequestReference());
    }

    private boolean hasGeneralReferralInProgress(GeneralReferral generalReferral) {
        return generalReferral != null && generalReferral.getGeneralReferralType() != null;
    }
}
