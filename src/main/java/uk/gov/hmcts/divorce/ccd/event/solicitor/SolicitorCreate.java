package uk.gov.hmcts.divorce.ccd.event.solicitor;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.ClaimForCosts;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.CoApplicantServiceDetails;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.FinancialOrders;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.LanguagePreference;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.MarriageCertificateDetails;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.OtherLegalProceedings;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.SolAboutTheApplicant;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.SolAboutTheCoApplicant;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.SolAboutTheSolicitor;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.ccd.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.ccd.access.Permissions.READ;
import static uk.gov.hmcts.divorce.ccd.access.Permissions.READ_UPDATE;
import static uk.gov.hmcts.divorce.ccd.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;

public class SolicitorCreate implements CcdConfiguration {

    public static final String SOLICITOR_CREATE = "solicitor-create";

    private final List<CcdPageConfiguration> pages = asList(
        new SolAboutTheSolicitor(),
        new SolAboutTheApplicant(),
        new SolAboutTheCoApplicant(),
        new CoApplicantServiceDetails(),
        new MarriageCertificateDetails(),
        new OtherLegalProceedings(),
        new FinancialOrders(),
        new ClaimForCosts(),
        new LanguagePreference()
    );

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        final FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder =
            addEventConfig(configBuilder);

        pages.forEach(page -> page.addTo(fieldCollectionBuilder));
    }

    private FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return configBuilder
            .event(SOLICITOR_CREATE)
            .initialState(SOTAgreementPayAndSubmitRequired)
            .name("Apply for a divorce")
            .description("Apply for a divorce")
            .displayOrder(1)
            .showSummary()
            .endButtonLabel("Save Petition")
            .aboutToStartWebhook(SOLICITOR_CREATE)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_DIVORCE_SOLICITOR)
            .grant(READ_UPDATE, CASEWORKER_DIVORCE_SUPERUSER)
            .grant(READ, CASEWORKER_DIVORCE_COURTADMIN_BETA, CASEWORKER_DIVORCE_COURTADMIN, CASEWORKER_DIVORCE_COURTADMIN_LA)
            .fields();
    }
}
