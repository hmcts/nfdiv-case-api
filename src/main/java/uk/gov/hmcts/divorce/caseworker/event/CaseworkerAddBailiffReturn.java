package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.SetHoldingDueDate;
import uk.gov.hmcts.divorce.citizen.notification.BailiffServiceSuccessfulNotification;
import uk.gov.hmcts.divorce.citizen.notification.BailiffServiceUnsuccessfulNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Bailiff;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJsNullity;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.IssuedToBailiff;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Component
@Slf4j
public class CaseworkerAddBailiffReturn implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_ADD_BAILIFF_RETURN = "caseworker-add-bailiff-return";

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private BailiffServiceUnsuccessfulNotification unsuccessfulNotification;

    @Autowired
    private BailiffServiceSuccessfulNotification successfulNotification;

    @Autowired
    private SetHoldingDueDate setHoldingDueDate;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_ADD_BAILIFF_RETURN)
            .forStates(
                IssuedToBailiff,
                AwaitingAos,
                AosOverdue,
                AosDrafted,
                Holding,
                Submitted,
                AwaitingDocuments,
                AwaitingPayment)
            .name("Add bailiff return")
            .description("Add bailiff return")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER, LEGAL_ADVISOR)
            .grantHistoryOnly(SUPER_USER, JUDGE, SOLICITOR, CITIZEN))
            .page("addBailiffReturn")
            .pageLabel("Add Bailiff Return")
            .complex(CaseData::getAlternativeService)
                .complex(AlternativeService::getBailiff)
                    .mandatory(Bailiff::getCertificateOfServiceDate)
                    .mandatory(Bailiff::getSuccessfulServedByBailiff)
                    .mandatory(Bailiff::getReasonFailureToServeByBailiff, "successfulServedByBailiff=\"No\"")
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        final Long caseId = details.getId();
        CaseData caseData = details.getData();
        final State state;

        log.info("Caseworker add bailiff return about to submit callback invoked for case id: {}", caseId);

        if (YES == caseData.getAlternativeService().getBailiff().getSuccessfulServedByBailiff()) {
            if (caseData.isJudicialSeparationCase()) {
                log.info("Setting state to AwaitingJsNullity for case id: {}", caseId);
                state = AwaitingJsNullity;
            } else {
                log.info("Setting state to Holding and due date for case id: {}", caseId);
                state = Holding;
                caseData = caseTasks(setHoldingDueDate).run(details).getData();
            }
            notificationDispatcher.send(successfulNotification, caseData, caseId);
        } else {
            log.info("Setting state to AwaitingAos for case id: {}", caseId);
            state = AwaitingAos;
            notificationDispatcher.send(unsuccessfulNotification, caseData, caseId);
        }

        caseData.archiveAlternativeServiceApplicationOnCompletion();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }
}
