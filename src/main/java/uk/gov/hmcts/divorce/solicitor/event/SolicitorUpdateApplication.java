package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
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
import uk.gov.hmcts.divorce.solicitor.service.SolicitorUpdateApplicationService;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ_UPDATE;

@Slf4j
@Component
public class SolicitorUpdateApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_UPDATE = "solicitor-update-application";

    @Autowired
    private SolAboutTheSolicitor solAboutTheSolicitor;

    @Autowired
    private SolicitorUpdateApplicationService solicitorUpdateApplicationService;

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
            new OtherLegalProceedings(),
            new FinancialOrders(),
            new UploadDocument(),
            new LanguagePreference(),
            new JurisdictionApplyForDivorce()
        );

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Solicitor update application about to submit callback invoked");

        final CaseDetails<CaseData, State> result = solicitorUpdateApplicationService.aboutToSubmit(details);

        //sort app1 documents in descending order so latest appears first
        result.getData().sortApplicant1UploadedDocuments(beforeDetails.getData().getApplicant1DocumentsUploaded());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(result.getData())
            .build();
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_UPDATE)
            .forState(Draft)
            .name("Amend divorce application")
            .description("Amend divorce application")
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grant(READ_UPDATE, CASEWORKER_SUPERUSER)
            .grant(READ,
                CASEWORKER_COURTADMIN,
                CASEWORKER_LEGAL_ADVISOR));
    }
}
