package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.SolUpdateContactDetails;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ_UPDATE;

@Component
public class SolicitorUpdateContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_UPDATE_CONTACT_DETAILS = "solicitor-update-contact-details";

    @Autowired
    private SolUpdateContactDetails solUpdateContactDetails;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        solUpdateContactDetails.addTo(pageBuilder);
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_UPDATE_CONTACT_DETAILS)
            .forState(Submitted)
            .name("Update contact details")
            .description("Update contact details")
            .showSummary()
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grant(READ_UPDATE, CASEWORKER_SUPERUSER)
            .grant(READ,
                CASEWORKER_COURTADMIN_CTSC,
                CASEWORKER_COURTADMIN_RDU,
                CASEWORKER_LEGAL_ADVISOR));
    }
}
