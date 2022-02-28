package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerUpdateContactDetails implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPDATE_CONTACT_DETAILS = "caseworker-update-contact-details";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_CONTACT_DETAILS)
            .forAllStates()
            .name("Update contact details")
            .description("Update contact details")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER))
            .page("updateContactDetails")
            .pageLabel("Update contact details");
    }
}
