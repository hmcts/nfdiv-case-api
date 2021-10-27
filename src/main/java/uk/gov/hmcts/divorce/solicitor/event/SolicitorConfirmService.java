package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.SolConfirmService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitConfirmService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Slf4j
@Component
public class SolicitorConfirmService implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_CONFIRM_SERVICE = "solicitor-confirm-service";

    @Autowired
    private SolicitorSubmitConfirmService solicitorSubmitConfirmService;

    private final List<CcdPageConfiguration> pages = List.of(
        new SolConfirmService()
    );

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();

        log.info("Solicitor confirm service about to submit callback invoked with service method {}",
            caseData.getApplication().getSolServiceMethod().toString());

        final List<String> validationErrors = validateConfirmService(caseData.getApplication());

        if (!validationErrors.isEmpty()) {
            log.info("ConfirmService Validation errors: ");
            for (String error : validationErrors) {
                log.info(error);
            }

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(validationErrors)
                .state(details.getState())
                .build();
        }


        final CaseDetails<CaseData, State> updateDetails = solicitorSubmitConfirmService.submitConfirmService(details);

        log.info("Due date after submit Task is {}", updateDetails.getData().getDueDate());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updateDetails.getData())
            .state(updateDetails.getState())
            .build();
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(SOLICITOR_CONFIRM_SERVICE)
            .forStateTransition(AwaitingService, AwaitingAos)
            .name("Solicitor confirm service")
            .description("Solicitor confirm service")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grant(READ,
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR));
    }

    private List<String> validateConfirmService(Application application) {
        List<String> errors = new ArrayList<>();

        if (!application.isSolicitorServiceMethod()) {
            errors.add("This event can only be used for a case with Solicitor Service as the service method");
        }
        return errors;
    }
}
