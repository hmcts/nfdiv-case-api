package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2ServiceDetails;
import uk.gov.hmcts.divorce.solicitor.event.page.ClaimForCosts;
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
import uk.gov.hmcts.divorce.solicitor.event.page.UploadMarriageCertificate;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.model.State.Draft;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ_UPDATE;

@Slf4j
@Component
public class SolicitorCreateApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_CREATE = "solicitor-create-application";

    @Autowired
    private SolAboutTheSolicitor solAboutTheSolicitor;

    @Autowired
    private SolicitorCreateApplicationService solicitorCreateApplicationService;

    @Autowired
    private HttpServletRequest request;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        final List<CcdPageConfiguration> pages = asList(
            new SolHowDoYouWantToApplyForDivorce(),
            solAboutTheSolicitor,
            new SolAboutApplicant1(),
            new SolAboutApplicant2(),
            new Applicant2ServiceDetails(),
            new MarriageCertificateDetails(),
            new OtherLegalProceedings(),
            new FinancialOrders(),
            new ClaimForCosts(),
            new UploadMarriageCertificate(),
            new LanguagePreference(),
            new JurisdictionApplyForDivorce(),
            new MarriageIrretrievablyBroken()
        );

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Solicitor create application about to submit callback invoked");

        final CaseData data = solicitorCreateApplicationService.aboutToSubmit(
            details.getData(),
            details.getId(),
            details.getCreatedDate().toLocalDate(),
            request.getHeader(AUTHORIZATION)
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    private PageBuilder addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_CREATE)
            .initialState(Draft)
            .name("Apply for a divorce")
            .description("Apply for a divorce")
            .displayOrder(1)
            .showSummary()
            .endButtonLabel("Save Application")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_DIVORCE_SOLICITOR)
            .grant(READ_UPDATE, CASEWORKER_DIVORCE_SUPERUSER)
            .grant(READ, CASEWORKER_DIVORCE_COURTADMIN_BETA, CASEWORKER_DIVORCE_COURTADMIN, CASEWORKER_DIVORCE_COURTADMIN_LA));
    }
}
