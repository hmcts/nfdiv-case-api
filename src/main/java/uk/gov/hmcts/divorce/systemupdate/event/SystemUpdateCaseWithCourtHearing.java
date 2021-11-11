package uk.gov.hmcts.divorce.systemupdate.event;

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
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Component
@Slf4j
public class SystemUpdateCaseWithCourtHearing implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_UPDATE_CASE_COURT_HEARING = "system-update-case-court-hearing";

    @Autowired
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SYSTEM_UPDATE_CASE_COURT_HEARING)
            .forStates(AwaitingPronouncement)
            .name("Update case with court hearing")
            .description("Update case with court hearing")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grant(READ, SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("System update case court hearing about to submit callback invoked for case id: {}", details.getId());

        final CaseDetails<CaseData, State> updatedDetails = caseTasks(generateCertificateOfEntitlement).run(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updatedDetails.getData())
            .build();
    }
}
