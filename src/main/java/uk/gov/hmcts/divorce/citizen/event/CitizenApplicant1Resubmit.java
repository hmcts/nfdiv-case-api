package uk.gov.hmcts.divorce.citizen.event;

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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateApplicant1BasicCase;

@Slf4j
@Component
public class CitizenApplicant1Resubmit implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_1_RESUBMIT = "applicant1-resubmit";

    @Autowired
    private Applicant1ResubmitNotification applicant1ResubmitNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(APPLICANT_1_RESUBMIT)
            .forStates(AwaitingApplicant1Response)
            .name("Applicant 1 Resubmit")
            .description("Applicant 1 resubmits for joint application")
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen applicant 1 Resubmits answers about to submit callback invoked");

        CaseData data = details.getData();

        log.info("Validating case data");
        final List<String> validationErrors = validate(data);

        if (!validationErrors.isEmpty()) {
            log.info("Validation errors: {} ", validationErrors);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(validationErrors)
                .state(AwaitingApplicant1Response)
                .build();
        }

        data.setDueDate(LocalDate.now().plus(2, ChronoUnit.WEEKS));

        applicant1ResubmitNotification.sendToApplicant1(data, details.getId());
        applicant1ResubmitNotification.sendToApplicant2(data, details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(AwaitingApplicant2Response)
            .build();
    }

    private List<String> validate(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        validateApplicant1BasicCase(caseData, errors);
        return errors;
    }
}

