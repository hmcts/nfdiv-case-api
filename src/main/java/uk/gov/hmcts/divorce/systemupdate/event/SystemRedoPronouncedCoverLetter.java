package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.task.SendRegeneratedCOPronouncedCoverLetters;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.systemupdate.service.task.RegenerateConditionalOrderPronouncedCoverLetterOffline;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Component
@Slf4j
public class SystemRedoPronouncedCoverLetter implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_REDO_PRONOUNCED_COVER_LETTER = "system-redo-pronounced-letter";

    @Autowired
    private RegenerateConditionalOrderPronouncedCoverLetterOffline regenerateCoverLettersOffline;

    @Autowired
    private SendRegeneratedCOPronouncedCoverLetters sendRegeneratedCoverLetters;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(
            configBuilder
                .event(SYSTEM_REDO_PRONOUNCED_COVER_LETTER)
                .forStates(AwaitingFinalOrder,
                    ConditionalOrderPronounced)
                .name("Resend CO Pronounced letter")
                .description("Resend CO Pronounced letter")
                .showCondition(NEVER_SHOW)
                .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
                .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR)
                .aboutToSubmitCallback(this::aboutToSubmit)
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final Long caseId = details.getId();

        log.info("SystemRedoPronouncedCoverLetter about to submit callback invoked for case id: {}", caseId);

        final CaseDetails<CaseData, State> response
            = caseTasks(
                regenerateCoverLettersOffline,
                sendRegeneratedCoverLetters
            )
            .run(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(response.getData())
            .build();
    }
}
