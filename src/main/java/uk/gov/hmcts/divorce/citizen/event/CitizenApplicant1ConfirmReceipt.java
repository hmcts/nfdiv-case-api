package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class CitizenApplicant1ConfirmReceipt implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_1_CONFIRM_RECEIPT = "applicant1-confirm-receipt";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(APPLICANT_1_CONFIRM_RECEIPT)
            .forStates(Holding)
            .name("Applicant 1 Confirm Receipt")
            .description("Applicant 1 confirms receipt for joint application")
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .grant(READ, SUPER_USER);
    }
}

