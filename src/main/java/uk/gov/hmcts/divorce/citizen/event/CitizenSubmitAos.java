package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.SoleAosSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingDispute;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Slf4j
@Component
public class CitizenSubmitAos implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_SUBMIT_AOS = "citizen-submit-aos";

    @Value("${submit_aos.dispute_offset_days}")
    private int disputeOffsetDays;

    @Value("${submit_aos.holding_offset_days}")
    private int holdingOffsetDays;

    @Autowired
    private SoleAosSubmittedNotification soleAosSubmittedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_SUBMIT_AOS)
            .forState(AosDrafted)
            .name("Respondent Statement of Truth")
            .description("The respondent confirms SOT")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, APPLICANT_2)
            .grant(READ, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit AOS application about to submit callback invoked");

        CaseData data = details.getData();
        AcknowledgementOfService acknowledgementOfService = data.getAcknowledgementOfService();
        State state = details.getState();

        log.info("Validating case data");
        final List<String> validationErrors = validateAos(acknowledgementOfService);

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

        if (data.getAcknowledgementOfService().getDisputeApplication() == YesOrNo.YES) {
            data.setDueDate(data.getApplication().getIssueDate().plusDays(disputeOffsetDays));
            soleAosSubmittedNotification.sendApplicationDisputedToApplicant(data, details.getId());
            soleAosSubmittedNotification.sendApplicationDisputedToRespondent(data, details.getId());
            state = PendingDispute;
        } else {
            data.setDueDate(data.getApplication().getIssueDate().plusDays(holdingOffsetDays));
            soleAosSubmittedNotification.sendApplicationNotDisputedToApplicant(data, details.getId());
            soleAosSubmittedNotification.sendApplicationNotDisputedToRespondent(data, details.getId());
            state = Holding;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(state)
            .build();
    }

    private List<String> validateAos(final AcknowledgementOfService acknowledgementOfService) {

        final List<String> errors = new ArrayList<>();

        if (NO.equals(acknowledgementOfService.getStatementOfTruth())) {
            errors.add("You must be authorised by the respondent to sign this statement.");
        }

        if (NO.equals(acknowledgementOfService.getConfirmReadPetition())) {
            errors.add("The respondent must have read the application for divorce.");
        }

        return errors;
    }

}
