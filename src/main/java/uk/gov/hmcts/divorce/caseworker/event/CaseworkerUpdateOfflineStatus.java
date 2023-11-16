package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerUpdateOfflineStatus implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_UPDATE_OFFLINE_STATUS = "caseworker-update-offline-status";

    private static final String NEVER_SHOW = "applicant1Offline=\"NEVER_SHOW\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_OFFLINE_STATUS)
            .forStates(POST_SUBMISSION_STATES)
            .name("Update offline status")
            .description("Update applicant offline status")
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER)
            .grantHistoryOnly(
                CASE_WORKER,
                SOLICITOR,
                LEGAL_ADVISOR))
            .page("updateOfflineStatus")
            .pageLabel("Update offline status")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getTheApplicantOrApplicant1, NEVER_SHOW)
                .readonlyNoSummary(LabelContent::getTheApplicant2, NEVER_SHOW)
            .done()
            .complex(CaseData::getApplicant1)
                .optionalWithLabel(Applicant::getOffline, "Is ${labelContentTheApplicantOrApplicant1} offline?")
            .done()
            .complex(CaseData::getApplicant2)
                .optionalWithLabel(Applicant::getOffline, "Is ${labelContentTheApplicant2} offline?")
            .done();
    }
}
