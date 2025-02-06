package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.ServiceApplicationNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.BailiffNotApprovedOrderContent;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.BailiffRefused;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_APPLICATION_NOT_APPROVED_FILE_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_APPLICATION_NOT_APPROVED_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.BAILIFF_SERVICE_REFUSED;

@Slf4j
@Component
public class CaseworkerConfirmBailiffRefusal implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_CONFIRM_BAILIFF_REFUSAL = "caseworker-confirm-bailiff-refusal";

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private BailiffNotApprovedOrderContent templateContentNotApproved;

    @Autowired
    private ServiceApplicationNotification serviceApplicationNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CONFIRM_BAILIFF_REFUSAL)
            .forState(BailiffRefused)
            .name("Confirm bailiff refusal")
            .description("Confirm bailiff refusal")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(
                SOLICITOR,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE)
            .aboutToSubmitCallback(this::aboutToSubmit));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker confirm bailiff refusal about to submit callback invoked for Case Id: {}", details.getId());

        var caseDataCopy = details.getData().toBuilder().build();

        var caseId = details.getId();

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseDataCopy,
            BAILIFF_SERVICE_REFUSED,
            templateContentNotApproved.apply(caseDataCopy, caseId),
            caseId,
            BAILIFF_APPLICATION_NOT_APPROVED_ID,
            caseDataCopy.getApplicant1().getLanguagePreference(),
            BAILIFF_APPLICATION_NOT_APPROVED_FILE_NAME
        );

        log.info("Sending ServiceApplicationNotification (refused) case ID: {}", details.getId());
        notificationDispatcher.send(serviceApplicationNotification, caseDataCopy, details.getId());

        log.info("Archiving service application for case ID: {}", details.getId());
        caseDataCopy.archiveAlternativeServiceApplicationOnCompletion();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .state(AwaitingAos)
            .build();
    }
}
