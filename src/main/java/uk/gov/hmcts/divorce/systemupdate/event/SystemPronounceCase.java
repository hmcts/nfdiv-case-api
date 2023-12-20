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
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.EnumSet;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.http.util.TextUtils.isBlank;
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
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CO_GRANTED_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemPronounceCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_PRONOUNCE_CASE = "system-pronounce-case";
    private final DocumentGenerator documentGenerator;
    private final ConditionalOrderPronouncedNotification conditionalOrderPronouncedNotification;
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
                .submittedCallback(this::submitted)
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final Long caseId = details.getId();

        log.info("Conditional order pronounced for Case({})", caseId);

        final State state = caseData.isJudicialSeparationCase() ? SeparationOrderGranted : ConditionalOrderPronounced;

        generateConditionalOrderGrantedDocs(details, beforeDetails);

        if (caseData.getDocuments().getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED).isPresent()) {
            log.info("Regenerating CO Pronounced document for Case Id: {}", details.getId());
            documentGenerator.generateAndStoreCaseDocument(
                CONDITIONAL_ORDER_GRANTED,
                CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID,
                CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME,
                caseData,
                details.getId()
            );
        }

        notificationDispatcher.send(conditionalOrderPronouncedNotification, caseData, details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .state(state)
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {
        log.info("SystemPronounceCase submitted callback invoked for case id: {}", details.getId());

        try {
            notificationDispatcher.send(conditionalOrderPronouncedNotification, details.getData(), details.getId());
        } catch (final NotificationTemplateException e) {
            log.error("Notification failed with message: {}", e.getMessage(), e);
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private void generateConditionalOrderGrantedDocs(final CaseDetails<CaseData, State> details,
                                                     final CaseDetails<CaseData, State> beforeDetails) {

        final CaseData newCaseData = details.getData();

        removeExistingAndGenerateNewConditionalOrderGrantedCoverLetters(newCaseData, details.getId());

        if (newCaseData.getDocuments().getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED).isPresent()) {
            ConditionalOrder oldCO = beforeDetails.getData().getConditionalOrder();
            ConditionalOrder newCO = newCaseData.getConditionalOrder();

            if (!newCO.getPronouncementJudge().equals(oldCO.getPronouncementJudge())
                || !newCO.getCourt().equals(oldCO.getCourt())
                || !newCO.getDateAndTimeOfHearing().equals(oldCO.getDateAndTimeOfHearing())) {

                removeExistingAndGenerateNewConditionalOrderGrantedDoc(details);
            }

        } else {
            removeExistingAndGenerateNewConditionalOrderGrantedDoc(details);
        }
    }

    private void removeExistingAndGenerateNewConditionalOrderGrantedCoverLetters(CaseData caseData, long caseId) {

        final List<DocumentType> documentTypesToRemove =
            List.of(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2);

        if (!isEmpty(caseData.getDocuments().getDocumentsGenerated())) {
            caseData.getDocuments().getDocumentsGenerated()
                .removeIf(document -> documentTypesToRemove.contains(document.getValue().getDocumentType()));
        }

        if (caseData.getApplicant1().isApplicantOffline()) {
            var app1 = caseData.getApplicant1();

            documentGenerator.generateAndStoreCaseDocument(
                CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1,
                CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
                CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME,
                caseData,
                caseId,
                app1);
        }

        if (caseData.getApplicant2().isApplicantOffline() || isBlank(caseData.getApplicant2EmailAddress())) {
            var app2 = caseData.getApplicant2();

            documentGenerator.generateAndStoreCaseDocument(
                CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2,
                CO_GRANTED_COVER_LETTER_TEMPLATE_ID,
                CONDITIONAL_ORDER_GRANTED_COVERSHEET_DOCUMENT_NAME,
                caseData,
                caseId,
                app2);
        }
    }
    private void removeExistingAndGenerateNewConditionalOrderGrantedDoc(CaseDetails<CaseData, State> caseDetails) {
        final CaseData caseData = caseDetails.getData();

        if (!isEmpty(caseData.getDocuments().getDocumentsGenerated())) {
            caseData.getDocuments().getDocumentsGenerated()
                .removeIf(document -> CONDITIONAL_ORDER_GRANTED.equals(document.getValue().getDocumentType()));
        }

        documentGenerator.generateAndStoreCaseDocument(
            CONDITIONAL_ORDER_GRANTED,
            CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID,
            CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME,
            caseData,
            caseDetails.getId());
    }

}
