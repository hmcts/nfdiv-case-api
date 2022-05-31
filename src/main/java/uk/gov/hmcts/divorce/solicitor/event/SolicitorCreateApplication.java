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
import uk.gov.hmcts.divorce.solicitor.event.page.MarriageCertificateDetails;
import uk.gov.hmcts.divorce.solicitor.event.page.MarriageIrretrievablyBroken;
import uk.gov.hmcts.divorce.solicitor.event.page.OtherLegalProceedings;
import uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant1;
import uk.gov.hmcts.divorce.solicitor.event.page.SolAboutApplicant2;
import uk.gov.hmcts.divorce.solicitor.event.page.SolAboutTheSolicitor;
import uk.gov.hmcts.divorce.solicitor.event.page.SolHowDoYouWantToApplyForDivorce;
import uk.gov.hmcts.divorce.solicitor.event.page.UploadDocument;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

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

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

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
            new UploadDocument()
        );

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Solicitor create application about to submit callback invoked for Case Id: {}", details.getId());

        if (Objects.isNull(details.getData().getApplicationType())) {
            log.error("Application type must be selected (cannot be null)");
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of("Application type must be selected (cannot be null)"))
                .build();
        }

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
            .name("Apply: divorce or dissolution")
            .description("Apply: divorce or dissolution")
            .showSummary()
            .showEventNotes()
            .endButtonLabel("Save Application")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, updatedRoles.toArray(UserRole[]::new))
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR));
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> before) {
        var orgId = details
            .getData()
            .getApplicant1()
            .getSolicitor()
            .getOrganisationPolicy()
            .getOrganisation()
            .getOrganisationId();

        log.info("Adding the applicant's solicitor case roles");
        ccdAccessService.addApplicant1SolicitorRole(
            httpServletRequest.getHeader(AUTHORIZATION),
            details.getId(),
            orgId
        );

        return SubmittedCallbackResponse.builder().build();
    }
}
