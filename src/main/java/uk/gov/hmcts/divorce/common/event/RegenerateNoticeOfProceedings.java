package uk.gov.hmcts.divorce.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant1NoticeOfProceeding;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant2NoticeOfProceedings;
import uk.gov.hmcts.divorce.caseworker.service.task.SetNoticeOfProceedingDetailsForRespondent;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@RequiredArgsConstructor
@Slf4j
@Component
public class RegenerateNoticeOfProceedings implements CCDConfig<CaseData, State, UserRole> {

    public static final String REGENERATE_NOTICE_OF_PROCEEDINGS = "regenerate-notice-of-proceedings";

    private final SetNoticeOfProceedingDetailsForRespondent setNoticeOfProceedingDetailsForRespondent;
    private final GenerateApplicant1NoticeOfProceeding generateApplicant1NoticeOfProceeding;
    private final GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(REGENERATE_NOTICE_OF_PROCEEDINGS)
            .forAllStates()
            .name("Regenerate NoP")
            .description("Regenerate NoP")
            .grant(CREATE_READ_UPDATE, CREATOR, SYSTEMUPDATE)
            .grantHistoryOnly(LEGAL_ADVISOR, SUPER_USER, JUDGE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", REGENERATE_NOTICE_OF_PROCEEDINGS, details.getId());

        final CaseDetails<CaseData, State> result = caseTasks(
            setNoticeOfProceedingDetailsForRespondent,
            generateApplicant1NoticeOfProceeding,
            generateApplicant2NoticeOfProceedings
        ).run(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(result.getData())
            .build();
    }
}
