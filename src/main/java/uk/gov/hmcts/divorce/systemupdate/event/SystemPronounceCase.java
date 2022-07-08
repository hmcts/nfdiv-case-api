package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.ConditionalOrderPronouncedNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedDocument;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class SystemPronounceCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_PRONOUNCE_CASE = "system-pronounce-case";

    @Autowired
    private ConditionalOrderPronouncedNotification conditionalOrderPronouncedNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private GenerateConditionalOrderPronouncedDocument generateDocument;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(
            configBuilder
                .event(SYSTEM_PRONOUNCE_CASE)
                .forStateTransition(AwaitingPronouncement, ConditionalOrderPronounced)
                .showCondition(NEVER_SHOW)
                .name("System pronounce case")
                .description("System pronounce case")
                .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
                .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR)
                .aboutToSubmitCallback(this::aboutToSubmit)
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final Long caseId = details.getId();

        log.info("Conditional order pronounced for Case({})", caseId);

        generateConditionalOrderGrantedDoc(details, beforeDetails);

        try {
            notificationDispatcher.send(conditionalOrderPronouncedNotification, caseData, caseId);
        } catch (final NotificationTemplateException e) {
            log.error("Notification failed with message: {}", e.getMessage(), e);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void generateConditionalOrderGrantedDoc(CaseDetails<CaseData, State> details,
                                                    CaseDetails<CaseData, State> beforeDetails) {
        if (generateDocument.getConditionalOrderGrantedDoc(details.getData()).isPresent()) {
            ConditionalOrder oldCO = beforeDetails.getData().getConditionalOrder();
            ConditionalOrder newCO = details.getData().getConditionalOrder();

            if (!newCO.getPronouncementJudge().equals(oldCO.getPronouncementJudge())
                || !newCO.getCourt().equals(oldCO.getCourt())
                || !newCO.getDateAndTimeOfHearing().equals(oldCO.getDateAndTimeOfHearing())) {
                generateDocument.removeExistingAndGenerateNewConditionalOrderGrantedDoc(details);
            }

        } else {
            generateDocument.apply(details);
        }
    }

}
