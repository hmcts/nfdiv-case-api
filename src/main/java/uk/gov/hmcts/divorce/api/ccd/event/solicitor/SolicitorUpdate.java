package uk.gov.hmcts.divorce.api.ccd.event.solicitor;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.divorce.api.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.LanguagePreference;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.SolAboutThePetitioner;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.SolAboutTheRespondent;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.SolAboutTheSolicitor;
import uk.gov.hmcts.divorce.api.ccd.model.CaseData;
import uk.gov.hmcts.divorce.api.ccd.model.State;
import uk.gov.hmcts.divorce.api.ccd.model.UserRole;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.api.ccd.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.api.ccd.access.Permissions.READ;
import static uk.gov.hmcts.divorce.api.ccd.access.Permissions.READ_UPDATE;
import static uk.gov.hmcts.divorce.api.ccd.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.api.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;

@Component
public class SolicitorUpdate implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_UPDATE = "solicitor-update";

    private final List<CcdPageConfiguration> pages = asList(
        new SolAboutTheSolicitor(),
        new SolAboutThePetitioner(),
        new SolAboutTheRespondent(),
        new LanguagePreference());

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        final FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder =
            addEventConfig(configBuilder);

        pages.forEach(page -> page.addTo(fieldCollectionBuilder));
    }

    private FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return configBuilder
            .event(SOLICITOR_UPDATE)
            .forState(SOTAgreementPayAndSubmitRequired)
            .name("Amend divorce application")
            .description("Amend divorce application")
            .displayOrder(2)
            .showSummary()
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_DIVORCE_SOLICITOR)
            .grant(READ_UPDATE, CASEWORKER_DIVORCE_SUPERUSER)
            .grant(READ, CASEWORKER_DIVORCE_COURTADMIN_BETA, CASEWORKER_DIVORCE_COURTADMIN, CASEWORKER_DIVORCE_COURTADMIN_LA)
            .fields();
    }
}
