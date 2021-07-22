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
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolAosCosts;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolAosOtherProceedings;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolAosjurisdiction;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolConfirmContactDetails;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolReviewApplicant1Application;
import uk.gov.hmcts.divorce.solicitor.event.updater.AddMiniApplicationLink;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class SolicitorDraftAos implements CCDConfig<CaseData, State, UserRole> {
    public static final String SOLICITOR_DRAFT_AOS = "solicitor-draft-aos";

    @Autowired
    private AddMiniApplicationLink addMiniApplicationLink;

    @Autowired
    private Clock clock;

    private final List<CcdPageConfiguration> pages = asList(
        new Applicant2SolConfirmContactDetails(),
        new Applicant2SolReviewApplicant1Application(),
        new Applicant2SolAosjurisdiction(),
        new Applicant2SolAosOtherProceedings(),
        new Applicant2SolAosCosts()
    );

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(SOLICITOR_DRAFT_AOS)
            .forStateTransition(AwaitingAos, AosDrafted)
            .name("Draft AoS")
            .description("Draft Acknowledgement of Service")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary()
            .endButtonLabel("Save AoS Response")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grant(READ,
                CASEWORKER_COURTADMIN_CTSC,
                CASEWORKER_COURTADMIN_RDU,
                CASEWORKER_SUPERUSER,
                CASEWORKER_LEGAL_ADVISOR));
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        var caseData = details.getData();
        caseData.getAcknowledgementOfService().setDateAosSubmitted(LocalDateTime.now(clock));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        CaseData caseData = addMiniApplicationLink.update(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
