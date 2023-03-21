package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.Applicant1ResubmitNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateApplicant1BasicCase;

@Slf4j
@Component
public class Applicant1Resubmit implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_1_RESUBMIT = "applicant1-resubmit";

    @Autowired
    private Applicant1ResubmitNotification applicant1ResubmitNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(APPLICANT_1_RESUBMIT)
            .forStates(AwaitingApplicant1Response)
            .name("Resubmit Applicant 1 Answers")
            .description("Applicant 1 resubmits for joint application")
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR, CREATOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, JUDGE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("Citizen applicant 1 Resubmits answers about to submit callback invoked for Case Id: {}", details.getId());

        CaseData data = details.getData();

        log.info("Validating case data");
        final List<String> validationErrors = validateApplicant1BasicCase(data);

        if (!validationErrors.isEmpty()) {
            log.info("Validation errors: {} ", validationErrors);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(validationErrors)
                .state(AwaitingApplicant1Response)
                .build();
        }

        data.setDueDate(LocalDate.now().plus(4, ChronoUnit.WEEKS));
        data.getApplication().setApplicant2ConfirmApplicant1Information(null);
        data.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation(null);

        notificationDispatcher.send(applicant1ResubmitNotification, data, details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(AwaitingApplicant2Response)
            .build();
    }

}

