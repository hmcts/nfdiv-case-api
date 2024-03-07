package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.ConditionalOrderPronouncedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedDocument;
import uk.gov.hmcts.divorce.systemupdate.service.task.RemoveExistingConditionalOrderPronouncedDocument;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.SeparationOrderGranted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemPronounceCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_PRONOUNCE_CASE = "system-pronounce-case";
    private final ConditionalOrderPronouncedNotification conditionalOrderPronouncedNotification;
    private final GenerateConditionalOrderPronouncedDocument generateConditionalOrderPronouncedDocument;
    private final RemoveExistingConditionalOrderPronouncedDocument removeExistingConditionalOrderPronouncedDocument;
    private final NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(
            configBuilder
                .event(SYSTEM_PRONOUNCE_CASE)
                .forStates(EnumSet.of(AwaitingPronouncement, OfflineDocumentReceived, ConditionalOrderPronounced))
                .name("System pronounce case")
                .description("System pronounce case")
                .grant(CREATE_READ_UPDATE, SYSTEMUPDATE, SUPER_USER)
                .grantHistoryOnly(SOLICITOR, CASE_WORKER, LEGAL_ADVISOR, JUDGE)
                .aboutToSubmitCallback(this::aboutToSubmit)
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final Long caseId = details.getId();

        log.info("Conditional order pronounced for Case({})", caseId);

        generateConditionalOrderGrantedDocs(details, beforeDetails);
        notificationDispatcher.send(conditionalOrderPronouncedNotification, caseData, details.getId());

        final State state = caseData.isJudicialSeparationCase() ? SeparationOrderGranted : ConditionalOrderPronounced;

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .state(state)
            .data(caseData)
            .build();
    }

    private void generateConditionalOrderGrantedDocs(final CaseDetails<CaseData, State> details,
                                                     final CaseDetails<CaseData, State> beforeDetails) {

        final CaseData newCaseData = details.getData();

        if (newCaseData.getDocuments().getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED).isPresent()) {
            ConditionalOrder oldCO = beforeDetails.getData().getConditionalOrder();
            ConditionalOrder newCO = newCaseData.getConditionalOrder();

            if (!newCO.getPronouncementJudge().equals(oldCO.getPronouncementJudge())
                || !newCO.getCourt().equals(oldCO.getCourt())
                || !newCO.getDateAndTimeOfHearing().equals(oldCO.getDateAndTimeOfHearing())) {

                caseTasks(
                    removeExistingConditionalOrderPronouncedDocument,
                    generateConditionalOrderPronouncedDocument
                ).run(details);
            }

        } else {
            caseTasks(generateConditionalOrderPronouncedDocument).run(details);
        }
    }

}
