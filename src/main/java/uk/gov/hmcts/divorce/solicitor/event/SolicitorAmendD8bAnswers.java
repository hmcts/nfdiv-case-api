package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD8bPage1;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD8bPage2;
import uk.gov.hmcts.divorce.solicitor.event.page.SolicitorAmendD8bApplicationActionPage;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.GENERAL_APPLICATION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorAmendD8bAnswers implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_AMEND_D8B_ANSWERS = "solicitor-amend-d8b-answers";

    private static final String AMEND_SHOW_CONDITION = "applicant2DraftApplicationAction=\"amend\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        new SolicitorAmendD8bApplicationActionPage().addTo(pageBuilder);

        List<CcdPageConfiguration> pages = List.of(
            new GeneralApplicationD8bPage1(),
            new GeneralApplicationD8bPage2()
        );

        pages.forEach(page -> page.addWithShowCondition(pageBuilder, AMEND_SHOW_CONDITION));
    }

    private PageBuilder addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_AMEND_D8B_ANSWERS)
            .forStates(GENERAL_APPLICATION_STATES)
            .name("Amend General Application")
            .description("Amend General Application")
            .showSummary()
            .showEventNotes()
            .showCondition("applicant2InterimApplicationType=\"solicitorD8BAnswers\"")
            .endButtonLabel("Submit")
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
