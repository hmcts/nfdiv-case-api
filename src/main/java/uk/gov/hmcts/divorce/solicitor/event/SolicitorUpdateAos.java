package uk.gov.hmcts.divorce.solicitor.event;

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
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolAosOtherProceedings;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolAosjurisdiction;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolUpdateAosApplicant1Application;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class SolicitorUpdateAos implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_UPDATE_AOS = "solicitor-update-aos";

    @Autowired
    private AddMiniApplicationLink addMiniApplicationLink;

    private final List<CcdPageConfiguration> pages = asList(
        new Applicant2SolUpdateAosApplicant1Application(),
        new Applicant2SolAosjurisdiction(),
        new Applicant2SolAosOtherProceedings()
    );

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(SOLICITOR_UPDATE_AOS)
            .forState(AosDrafted)
            .name("Update AoS")
            .description("Update Acknowledgement of Service")
            .aboutToStartCallback(this::aboutToStart)
            .showSummary()
            .endButtonLabel("Save Updated AoS Response")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grant(READ,
                CASEWORKER_COURTADMIN,
                CASEWORKER_SUPERUSER,
                CASEWORKER_LEGAL_ADVISOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(addMiniApplicationLink
                .apply(details)
                .getData())
            .build();
    }
}
