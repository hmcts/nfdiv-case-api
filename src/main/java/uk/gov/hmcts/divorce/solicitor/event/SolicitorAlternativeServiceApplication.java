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
import uk.gov.hmcts.divorce.solicitor.event.page.AlternativeServiceConfirmPage;
import uk.gov.hmcts.divorce.solicitor.event.page.AlternativeServicePaymentPage;
import uk.gov.hmcts.divorce.solicitor.event.page.SolicitorAlternativeServiceMethodPage;
import uk.gov.hmcts.divorce.solicitor.event.page.SolicitorAlternativeServiceReasonPage;

import java.util.EnumSet;
import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorAlternativeServiceApplication implements CCDConfig<CaseData, State, UserRole> {

    private static final String ALTERNATIVE_SERVICE = "Alternative Service App";
    private static final String SERVICE_TYPE_LABEL = "alternative";

    public static final String SOLICITOR_ALTERNATIVE_SERVICE_APPLICATION = "solicitor-alternative-service-application";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        final List<CcdPageConfiguration> pages = asList(
            new AlternativeServiceConfirmPage(),
            new AlternativeServicePaymentPage(),
            new SolicitorAlternativeServiceReasonPage(),
            new SolicitorAlternativeServiceMethodPage()
        );

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var states = EnumSet.complementOf(POST_SUBMISSION_STATES);
        states.add(AosOverdue);

        return new PageBuilder(configBuilder
                .event(SOLICITOR_ALTERNATIVE_SERVICE_APPLICATION)
                .forStates(states)
                .name(ALTERNATIVE_SERVICE)
                .description(ALTERNATIVE_SERVICE)
                .showSummary()
                .showEventNotes()
                .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
                .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER));
    }
}
