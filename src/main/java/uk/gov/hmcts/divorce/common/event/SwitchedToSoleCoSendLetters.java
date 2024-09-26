package uk.gov.hmcts.divorce.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.SwitchToSoleCODocumentPack;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.JSAwaitingLA;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@RequiredArgsConstructor
@Slf4j
@Component
public class SwitchedToSoleCoSendLetters implements CCDConfig<CaseData, State, UserRole> {

    public static final String SWITCH_TO_SOLE_CO_SEND_LETTERS = "switch-to-sole-co-send-letters";

    private final SwitchToSoleCODocumentPack switchToSoleConditionalOrderDocumentPack;

    private final LetterPrinter letterPrinter;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SWITCH_TO_SOLE_CO_SEND_LETTERS)
            .forStates(JSAwaitingLA, AwaitingLegalAdvisorReferral)
            .name("SwitchedToSoleCOSendLetters")
            .description("Switch to sole co send letters")
            .grant(CREATE_READ_UPDATE, CREATOR, SYSTEMUPDATE)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        Long caseId = details.getId();
        log.info("Switched To Sole CO Send Letters aboutToSubmit callback invoked for Case Id: {}", caseId);
        CaseData data = details.getData();

        var documentPackInfo = switchToSoleConditionalOrderDocumentPack.getDocumentPack(data, null);
        letterPrinter.sendLetters(
            data,
            caseId,
            data.getApplicant2(),
            documentPackInfo,
            switchToSoleConditionalOrderDocumentPack.getLetterId()
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
