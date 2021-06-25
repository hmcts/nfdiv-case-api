package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolAosCosts;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolAosOtherProceedings;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolAosjurisdiction;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolConfirmContactDetails;
import uk.gov.hmcts.divorce.solicitor.event.page.Applicant2SolReviewApplicant1Application;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.common.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;

@Component
public class SolicitorSubmitDraftAos implements CCDConfig<CaseData, State, UserRole> {
    public static final String SOLICITOR_DRAFT_AOS = "solicitor-draft-aos";

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
            .description("Draft AoS")
            .aboutToStartCallback(this::aboutToStart)
            .showSummary()
            .endButtonLabel("Save AOS Response")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SOLICITOR)
            .grant(READ,
                CASEWORKER_COURTADMIN_CTSC,
                CASEWORKER_COURTADMIN_RDU,
                CASEWORKER_SUPERUSER,
                CASEWORKER_LEGAL_ADVISOR));
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        CaseData caseData = details.getData();

        Stream.ofNullable(caseData.getDocumentsGenerated())
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .filter(divorceDocument ->
                DIVORCE_APPLICATION.equals(divorceDocument.getDocumentType()))
            .map(DivorceDocument::getDocumentLink)
            .findFirst()
            .ifPresent(caseData::setMiniApplicationLink);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
