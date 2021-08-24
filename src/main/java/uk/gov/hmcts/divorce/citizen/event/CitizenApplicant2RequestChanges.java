package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2RequestChangesNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateApplicant2RequestChanges;

@Slf4j
@Component
public class CitizenApplicant2RequestChanges implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_APPLICANT_2_REQUEST_CHANGES = "applicant2-request-changes";

    @Autowired
    private Applicant2RequestChangesNotification applicant2RequestChangesNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_APPLICANT_2_REQUEST_CHANGES)
            .forStateTransition(AwaitingApplicant2Response, AwaitingApplicant1Response)
            .name("Applicant 2 Request Changes")
            .description("Applicant 2 Requests changes to be made by Applicant 1")
            .grant(CREATE_READ_UPDATE, APPLICANT_2)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Citizen applicant 2 request changes about to submit callback invoked");
        CaseData data = details.getData();

        log.info("Validating case data");
        final List<String> validationErrors = validate(data);

        if (!validationErrors.isEmpty()) {
            log.info("Validation errors: {} ", validationErrors);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(validationErrors)
                .state(AwaitingApplicant2Response)
                .build();
        }

        applicant2RequestChangesNotification.sendToApplicant1(data, details.getId());
        applicant2RequestChangesNotification.sendToApplicant2(data, details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(AwaitingApplicant1Response)
            .build();
    }

    private List<String> validate(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        validateApplicant2RequestChanges(caseData, errors);
        return errors;
    }
}
