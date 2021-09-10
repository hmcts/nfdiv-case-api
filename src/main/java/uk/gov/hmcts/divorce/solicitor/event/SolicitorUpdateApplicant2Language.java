package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ_UPDATE;

@Component
public class SolicitorUpdateApplicant2Language implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_UPDATE_APPLICANT_2_LANGUAGE = "solicitor-update-applicant2-language";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SOLICITOR_UPDATE_APPLICANT_2_LANGUAGE)
            .forAllStates()
            .name("Update language")
            .description("Update language")
            .showSummary()
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grant(READ_UPDATE, CASEWORKER_SUPERUSER)
            .grant(READ,
                CASEWORKER_COURTADMIN,
                CASEWORKER_LEGAL_ADVISOR))
            .page("langPref2")
            .pageLabel("Select Language")
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getLanguagePreferenceWelsh)
                .done();
    }
}
