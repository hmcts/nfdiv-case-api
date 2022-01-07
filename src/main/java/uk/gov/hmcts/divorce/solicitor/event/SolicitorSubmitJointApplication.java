package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.FinancialOrdersForApplicant2;
import uk.gov.hmcts.divorce.solicitor.event.page.HelpWithFeesPageForApplicant2;
import uk.gov.hmcts.divorce.solicitor.event.page.MarriageIrretrievablyBrokenForApplicant2;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Slf4j
@Component
public class SolicitorSubmitJointApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_SUBMIT_JOINT_APPLICATION = "solicitor-submit-joint-application";

    @Autowired
    private MarriageIrretrievablyBrokenForApplicant2 marriageIrretrievablyBrokenForApplicant2;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final List<CcdPageConfiguration> pages = asList(
            marriageIrretrievablyBrokenForApplicant2,
            new FinancialOrdersForApplicant2(),
            new HelpWithFeesPageForApplicant2()
        );

        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder.event(SOLICITOR_SUBMIT_JOINT_APPLICATION)
            .forStates(AwaitingApplicant2Response, Draft)
            .name("Submit joint application")
            .description("Submit joint application")
            .showSummary(false)
            .endButtonLabel("Submit Application")
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR, SOLICITOR)
            .grant(READ,
                APPLICANT_1_SOLICITOR,
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR));
    }
}
