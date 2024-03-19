package uk.gov.hmcts.divorce.common.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.SwitchToSoleCoNotification;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.SwitchToSoleCODocumentPack;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.CO_D84;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D84;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineApplicationType.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.JSAwaitingLA;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class SwitchedToSoleCo implements CCDConfig<CaseData, State, UserRole> {

    public static final String SWITCH_TO_SOLE_CO = "switch-to-sole-co";

    private final CcdAccessService ccdAccessService;
    private final HttpServletRequest httpServletRequest;
    private final SwitchToSoleCoNotification switchToSoleCoNotification;
    private final NotificationDispatcher notificationDispatcher;
    private final SwitchToSoleService switchToSoleService;
    private final SwitchToSoleCODocumentPack switchToSoleConditionalOrderDocumentPack;
    private final LetterPrinter letterPrinter;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SWITCH_TO_SOLE_CO)
            .forStates(ConditionalOrderPending, AwaitingLegalAdvisorReferral, JSAwaitingLA)
            .name("SwitchedToSoleCO")
            .description("Application type switched to sole post CO submission")
            .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_2, SYSTEMUPDATE)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR)
            .retries(0, 0, 0)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        Long caseId = details.getId();
        log.info("SwitchedToSoleCO aboutToSubmit callback invoked for Case Id: {}", caseId);
        CaseData data = details.getData();

        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setSwitchedToSoleCo(YES);
        data.getLabelContent().setApplicationType(SOLE_APPLICATION);
        data.getConditionalOrder().setSwitchedToSole(YES);

        // triggered by citizen users
        if (ccdAccessService.isApplicant2(httpServletRequest.getHeader(AUTHORIZATION), caseId)) {
            switchToSoleService.switchUserRoles(data, caseId);
            switchToSoleService.switchApplicantData(data);
        }

        // triggered by system update user coming from Offline Document Verified
        if (OfflineWhoApplying.APPLICANT_2.equals(data.getConditionalOrder().getD84WhoApplying())) {
            if (!data.getApplication().isPaperCase()) {
                switchToSoleService.switchUserRoles(data, caseId);
            }
            switchToSoleService.switchApplicantData(data);
        }

        final var state = details.getState() == JSAwaitingLA ? JSAwaitingLA : AwaitingLegalAdvisorReferral;

        log.info("SwitchedToSoleCO submitted callback invoked for case id: {}", details.getId());

        notificationDispatcher.send(switchToSoleCoNotification, data, details.getId());

        if (CO_D84.equals(data.getDocuments().getTypeOfDocumentAttached())
            || D84.equals(data.getDocuments().getScannedSubtypeReceived())
            && SWITCH_TO_SOLE.equals(data.getConditionalOrder().getD84ApplicationType())) {

            var documentPackInfo =
                switchToSoleConditionalOrderDocumentPack.getDocumentPack(data, null);
            letterPrinter.sendLetters(
                data,
                caseId,
                data.getApplicant2(),
                documentPackInfo,
                switchToSoleConditionalOrderDocumentPack.getLetterId()
            );
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(state)
            .build();
    }
}
