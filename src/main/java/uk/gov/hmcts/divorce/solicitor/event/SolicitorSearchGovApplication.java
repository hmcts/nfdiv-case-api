package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.divorce.solicitor.event.page.searchgov.SearchGovAnyOtherAddressesPage;
import uk.gov.hmcts.divorce.solicitor.event.page.searchgov.SearchGovConfirmPage;
import uk.gov.hmcts.divorce.solicitor.event.page.searchgov.SearchGovLastKnownAddressPage;
import uk.gov.hmcts.divorce.solicitor.event.page.searchgov.SearchGovRespondentDetailsPage;
import uk.gov.hmcts.divorce.solicitor.event.page.searchgov.SearchGovWhichDepartmentsPage;
import uk.gov.hmcts.divorce.solicitor.service.GeneralApplicationDraftJourneyService;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.SOLICITOR_SERVICE_APPLICATION_AND_SEARCH_GOV_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorSearchGovApplication implements CCDConfig<CaseData, State, UserRole> {

    private final GeneralApplicationDraftJourneyService generalApplicationDraftJourneyService;

    public static final String SOLICITOR_SEARCH_GOV_APPLICATION = "solicitor-search-gov-application";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        final List<CcdPageConfiguration> pages = List.of(
            new SearchGovConfirmPage(),
            new SearchGovWhichDepartmentsPage(),
            new SearchGovRespondentDetailsPage(),
            new SearchGovLastKnownAddressPage(),
            new SearchGovAnyOtherAddressesPage());

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_SEARCH_GOV_APPLICATION, details.getId());
        final CaseData data = details.getData();

        generalApplicationDraftJourneyService.prepareAboutToSubmitForSearchGovRecords(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    private PageBuilder addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_SEARCH_GOV_APPLICATION)
            .forStates(SOLICITOR_SERVICE_APPLICATION_AND_SEARCH_GOV_STATES)
            .name("Draft Search Gov")
            .description("Draft Search Government Records")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .endButtonLabel("Save Application")
            .grant(CREATE_READ_UPDATE_DELETE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
