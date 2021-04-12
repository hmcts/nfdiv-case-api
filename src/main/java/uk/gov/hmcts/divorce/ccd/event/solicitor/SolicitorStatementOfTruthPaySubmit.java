package uk.gov.hmcts.divorce.ccd.event.solicitor;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.HelpWithFees;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.SolPayAccount;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.SolPayment;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.SolPaymentSummary;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.SolStatementOfTruth;
import uk.gov.hmcts.divorce.ccd.event.solicitor.page.SolSummary;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.ccd.model.State;
import uk.gov.hmcts.divorce.ccd.model.UserRole;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.ccd.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.ccd.access.Permissions.READ;
import static uk.gov.hmcts.divorce.ccd.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;

public class SolicitorStatementOfTruthPaySubmit implements CcdConfiguration {

    public static final String SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT = "solicitor-statement-of-truth-pay-submit";

    public static final String SUBMIT_PETITION = "submit-petition";

    private final List<CcdPageConfiguration> pages = asList(
        new SolStatementOfTruth(),
        new SolPayment(),
        new HelpWithFees(),
        new SolPayAccount(),
        new SolPaymentSummary(),
        new SolSummary());

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        final FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder =
            addEventConfig(configBuilder);

        pages.forEach(page -> page.addTo(fieldCollectionBuilder));
    }

    private FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return configBuilder.event(SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT)
            .forStates(SOTAgreementPayAndSubmitRequired)
            .name("Case submission")
            .description("Agree Statement of Truth, Pay & Submit")
            .displayOrder(1)
            .showSummary()
            .endButtonLabel("Submit Petition")
            .aboutToStartWebhook(SUBMIT_PETITION)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_DIVORCE_SOLICITOR)
            .grant(READ,
                CASEWORKER_DIVORCE_COURTADMIN_BETA,
                CASEWORKER_DIVORCE_COURTADMIN,
                CASEWORKER_DIVORCE_SUPERUSER,
                CASEWORKER_DIVORCE_COURTADMIN_LA)
            .fields();
    }
}
