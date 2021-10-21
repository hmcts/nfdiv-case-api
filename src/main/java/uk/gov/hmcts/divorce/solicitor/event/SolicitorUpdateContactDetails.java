package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.SolUpdateContactDetails;
import uk.gov.hmcts.divorce.solicitor.service.task.SetApplicant1SolicitorAddress;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Component
@Slf4j
public class SolicitorUpdateContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_UPDATE_CONTACT_DETAILS = "solicitor-update-contact-details";

    @Autowired
    private SolUpdateContactDetails solUpdateContactDetails;

    @Autowired
    private SetApplicant1SolicitorAddress setApplicant1SolicitorAddress;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder<CaseData, UserRole, State> pageBuilder = addEventConfig(configBuilder);
        solUpdateContactDetails.addTo(pageBuilder);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Solicitor update contact details about to submit callback invoked");

        final CaseDetails<CaseData, State> result = caseTasks(setApplicant1SolicitorAddress).run(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(result.getData())
            .build();
    }

    private PageBuilder<CaseData, UserRole, State> addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder<>(configBuilder
            .event(SOLICITOR_UPDATE_CONTACT_DETAILS)
            .forState(Submitted)
            .name("Update contact details")
            .description("Update contact details")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grant(READ_UPDATE, SUPER_USER)
            .grant(READ,
                CASE_WORKER,
                LEGAL_ADVISOR));
    }
}
