package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.ServiceApplicationNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.BailiffApprovedOrderContent;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.BailiffRefused;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_APPLICATION_APPROVED_FILE_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_APPLICATION_APPROVED_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.BAILIFF_SERVICE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerMakeBailiffDecision implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_BAILIFF_DECISION = "caseworker-bailiff-decision";

    private final CaseDataDocumentService caseDataDocumentService;

    private final BailiffApprovedOrderContent templateContent;

    private final Clock clock;

    private final ServiceApplicationNotification serviceApplicationNotification;

    private final NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_BAILIFF_DECISION)
            .forState(AwaitingBailiffReferral)
            .name("Make bailiff decision")
            .description("Make bailiff decision")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, LEGAL_ADVISOR)
            .grantHistoryOnly(CASE_WORKER, SOLICITOR, SYSTEMUPDATE, JUDGE))
            .page("makeBailiffDecision-1")
            .pageLabel("Make Bailiff Decision")
            .complex(CaseData::getAlternativeService)
                .mandatory(AlternativeService::getServiceApplicationGranted)
                .done()
            .page("makeBailiffDecision-2")
            .showCondition("serviceApplicationGranted=\"No\"")
            .pageLabel("Reason for refusal")
                .complex(CaseData::getAlternativeService)
                .mandatory(AlternativeService::getServiceApplicationRefusalReason)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker make bailiff decision about to submit callback invoked for Case Id: {}", details.getId());

        var caseDataCopy = details.getData().toBuilder().build();
        var serviceApplication = caseDataCopy.getAlternativeService();

        State endState;
        serviceApplication.setServiceApplicationDecisionDate(LocalDate.now(clock));

        var caseId = details.getId();
        if (serviceApplication.getServiceApplicationGranted().toBoolean()) {
            endState = AwaitingBailiffService;
            // ServiceApplication is archived after BailiffReturn if ServiceGranted is set to Yes

            caseDataDocumentService.renderDocumentAndUpdateCaseData(
                caseDataCopy,
                BAILIFF_SERVICE,
                templateContent.apply(caseDataCopy, caseId),
                caseId,
                BAILIFF_APPLICATION_APPROVED_ID,
                caseDataCopy.getApplicant1().getLanguagePreference(),
                BAILIFF_APPLICATION_APPROVED_FILE_NAME
            );

            log.info("Sending ServiceApplicationNotification (granted) case ID: {}", details.getId());
            notificationDispatcher.send(serviceApplicationNotification, caseDataCopy, details.getId());
        } else {
            endState = BailiffRefused;
        }

        log.info("Setting end state of case id {} to {}", details.getId(), endState);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .state(endState)
            .build();
    }
}
