package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class CaseworkerUpdateLanguage implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_UPDATE_LANGUAGE = "caseworker-update-language";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_LANGUAGE)
            .forAllStates()
            .name("Update Language")
            .description("Update Language")
            .showSummary(false)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE_DELETE, CASEWORKER_SUPERUSER)
            .grant(READ, SOLICITOR)
            .grant(CREATE_READ_UPDATE,
                CASEWORKER_COURTADMIN_CTSC,
                CASEWORKER_COURTADMIN_RDU,
                CASEWORKER_LEGAL_ADVISOR))
            .page("caseworkerLangPref")
            .pageLabel("Select Language")
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getLanguagePreferenceWelsh, null, null, "Applicant's language preference Welsh?")
                .done()
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getLanguagePreferenceWelsh, null, null, "Respondent's language preference Welsh?")
                .done();
    }
}
