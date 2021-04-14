package uk.gov.hmcts.divorce.api.ccd.event.solicitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.api.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.ClaimForCosts;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.FinancialOrders;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.LanguagePreference;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.MarriageCertificateDetails;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.OtherLegalProceedings;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.RespondentServiceDetails;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.SolAboutThePetitioner;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.SolAboutTheRespondent;
import uk.gov.hmcts.divorce.api.ccd.event.solicitor.page.SolAboutTheSolicitor;
import uk.gov.hmcts.divorce.api.ccd.model.CaseData;
import uk.gov.hmcts.divorce.api.ccd.model.State;
import uk.gov.hmcts.divorce.api.ccd.model.UserRole;
import uk.gov.hmcts.divorce.api.service.solicitor.SolicitorCreatePetitionService;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;
import static org.reflections.Reflections.log;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
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
public class SolicitorCreate implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_CREATE = "solicitor-create";

    private final List<CcdPageConfiguration> pages = asList(
        new SolAboutTheSolicitor(),
        new SolAboutThePetitioner(),
        new SolAboutTheRespondent(),
        new RespondentServiceDetails(),
        new MarriageCertificateDetails(),
        new OtherLegalProceedings(),
        new FinancialOrders(),
        new ClaimForCosts(),
        new LanguagePreference()
    );

    @Autowired
    private SolicitorCreatePetitionService solicitorCreatePetitionService;

    @Autowired
    HttpServletRequest request;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

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
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_DIVORCE_SOLICITOR)
            .grant(READ_UPDATE, CASEWORKER_DIVORCE_SUPERUSER)
            .grant(READ, CASEWORKER_DIVORCE_COURTADMIN_BETA, CASEWORKER_DIVORCE_COURTADMIN, CASEWORKER_DIVORCE_COURTADMIN_LA)
            .fields();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        log.info("Solicitor create petition about to start callback invoked");

        CaseData data = details.getData();
        data.setLanguagePreferenceWelsh(YesOrNo.NO);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                        CaseDetails<CaseData, State> beforeDetails) {
        log.info("Solicitor create petition about to submit callback invoked");

        final CaseData data = solicitorCreatePetitionService.aboutToSubmit(
            details.getData(),
            details.getId(),
            request.getHeader(AUTHORIZATION)
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
