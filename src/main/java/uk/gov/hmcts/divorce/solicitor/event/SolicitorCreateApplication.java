package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.AddSystemUpdateRole;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2ServiceDetails;
import uk.gov.hmcts.divorce.solicitor.event.page.FinancialOrders;
import uk.gov.hmcts.divorce.solicitor.event.page.JurisdictionApplyForDivorce;
import uk.gov.hmcts.divorce.solicitor.event.page.LanguagePreference;
import uk.gov.hmcts.divorce.solicitor.event.page.MarriageCertificateDetails;
import uk.gov.hmcts.divorce.solicitor.event.page.MarriageIrretrievablyBroken;
import uk.gov.hmcts.divorce.solicitor.event.page.OtherLegalProceedings;
import uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1;
import uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant2;
import uk.gov.hmcts.divorce.solicitor.event.page.SolAboutTheSolicitor;
import uk.gov.hmcts.divorce.solicitor.event.page.SolHowDoYouWantToApplyForDivorce;
import uk.gov.hmcts.divorce.solicitor.event.page.UploadDocument;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ_UPDATE;

@Slf4j
@Component
public class SolicitorCreateApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_CREATE = "solicitor-create-application";

    @Autowired
    private SolAboutTheSolicitor solAboutTheSolicitor;

    @Autowired
    private SolicitorCreateApplicationService solicitorCreateApplicationService;

    @Autowired
    private AddSystemUpdateRole addSystemUpdateRole;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        final List<CcdPageConfiguration> pages = asList(
            new SolHowDoYouWantToApplyForDivorce(),
            solAboutTheSolicitor,
            new MarriageIrretrievablyBroken(),
            new SolAboutApplicant1(),
            new SolAboutApplicant2(),
            new Applicant2ServiceDetails(),
            new MarriageCertificateDetails(),
            new JurisdictionApplyForDivorce(),
            new OtherLegalProceedings(),
            new FinancialOrders(),
            new UploadDocument(),
            new LanguagePreference()
        );

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Solicitor create application about to submit callback invoked");

        final CaseDetails<CaseData, State> result = solicitorCreateApplicationService.aboutToSubmit(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(result.getData())
            .build();
    }

    private PageBuilder addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        var defaultRoles = new ArrayList<UserRole>();
        defaultRoles.add(SOLICITOR);

        var updatedRoles = addSystemUpdateRole.addIfConfiguredForEnvironment(defaultRoles);

        return new PageBuilder(configBuilder
            .event(SOLICITOR_CREATE)
            .initialState(Draft)
            .name("Apply for a divorce")
            .description("Apply for a divorce")
            .showSummary()
            .endButtonLabel("Save Application")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, updatedRoles.toArray(UserRole[]::new))
            .grant(READ_UPDATE, SUPER_USER)
            .grant(READ, CASE_WORKER, LEGAL_ADVISOR));
    }
}
