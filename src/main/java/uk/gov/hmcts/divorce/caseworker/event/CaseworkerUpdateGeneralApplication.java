package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_ISSUE_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerUpdateGeneralApplication implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPDATE_GENERAL_APPLICATION = "caseworker-update-general-application";

    private static final String UPDATE_GENERAL_APPLICATION = "Update general application";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_GENERAL_APPLICATION)
            .forStates(POST_ISSUE_STATES)
            .name(UPDATE_GENERAL_APPLICATION)
            .description(UPDATE_GENERAL_APPLICATION)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(
                SUPER_USER,
                SOLICITOR,
                LEGAL_ADVISOR,
                JUDGE
                ))
            .page("updateGeneralApplication")
            .pageLabel(UPDATE_GENERAL_APPLICATION)
            .complex(CaseData::getGeneralApplication)
            .optional(GeneralApplication::getGeneralApplicationUrgentCase)
            .mandatory(GeneralApplication::getGeneralApplicationUrgentCaseReason, "generalApplicationUrgentCase=\"Yes\"")
            .done();
    }
}
