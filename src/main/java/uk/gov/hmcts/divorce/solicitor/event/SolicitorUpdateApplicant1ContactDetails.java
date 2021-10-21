package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.UpdateApplicant1ContactDetails;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ_UPDATE;

@Component
public class SolicitorUpdateApplicant1ContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_UPDATE_APPLICANT1_CONTACT_DETAILS = "solicitor-update-applicant1-contact-details";

    private final CcdPageConfiguration applicant1UpdateContactDetails = new UpdateApplicant1ContactDetails();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder<CaseData, UserRole, State> pageBuilder = addEventConfig(configBuilder);
        applicant1UpdateContactDetails.addTo(pageBuilder);
    }

    private PageBuilder<CaseData, UserRole, State> addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder<>(configBuilder
            .event(APPLICANT_UPDATE_APPLICANT1_CONTACT_DETAILS)
            .forAllStates()
            .name("Update applicant contact info")
            .description("Update applicant contact details")
            .showSummary()
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grant(READ_UPDATE, SUPER_USER)
            .grant(READ,
                CASE_WORKER,
                LEGAL_ADVISOR));
    }
}
