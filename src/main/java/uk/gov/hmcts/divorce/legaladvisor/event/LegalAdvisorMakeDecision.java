package uk.gov.hmcts.divorce.legaladvisor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.RefusalOption;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForAmendmentContent;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForClarificationContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.legaladvisor.notification.LegalAdvisorMoreInfoDecisionNotification;
import uk.gov.hmcts.divorce.legaladvisor.notification.LegalAdvisorRejectedDecisionNotification;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAdminClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAmendedApplication;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ClarificationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderReview;
import static uk.gov.hmcts.divorce.divorcecase.model.State.JSAwaitingLA;
import static uk.gov.hmcts.divorce.divorcecase.model.State.LAReview;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CLARIFICATION_REFUSAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REFUSAL_ORDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;

@Component
@Slf4j
public class LegalAdvisorMakeDecision implements CCDConfig<CaseData, State, UserRole> {

    public static final String LEGAL_ADVISOR_MAKE_DECISION = "legal-advisor-make-decision";

    @Autowired
    private LegalAdvisorRejectedDecisionNotification rejectedNotification;

    @Autowired
    private LegalAdvisorMoreInfoDecisionNotification moreInfoDecisionNotification;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private ConditionalOrderRefusedForAmendmentContent conditionalOrderRefusedForAmendmentContent;

    @Autowired
    private ConditionalOrderRefusedForClarificationContent conditionalOrderRefusedForClarificationContent;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(LEGAL_ADVISOR_MAKE_DECISION)
            .forStates(AwaitingLegalAdvisorReferral, JSAwaitingLA, ClarificationSubmitted, LAReview, ConditionalOrderReview)
            .name("Make a decision")
            .description("Grant Conditional Order")
            .endButtonLabel("Submit")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, LEGAL_ADVISOR)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                APPLICANT_1_SOLICITOR,
                JUDGE))
            .page("grantConditionalOrder")
            .pageLabel("Grant Conditional Order")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getGranted)
                .done()
            .page("makeRefusalOrder")
            .pageLabel("Make a refusal order")
            .showCondition("coGranted=\"No\"")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getRefusalDecision)
            .done()
            .page("refusalOrderClarification", this::midEvent)
            .pageLabel("Refusal Order:Clarify - Make a Decision")
            .showCondition("coRefusalDecision=\"moreInfo\" AND coGranted=\"No\"")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getRefusalClarificationReason)
                .mandatory(ConditionalOrder::getRefusalClarificationAdditionalInfo,
                    "coRefusalClarificationReasonCONTAINS \"other\"")
            .done()
            .page("adminErrorClarification")
            .pageLabel("Admin error - Make a Decision")
            .showCondition("coRefusalDecision=\"adminError\" AND coGranted=\"No\"")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getRefusalAdminErrorInfo)
            .done()
            .page("amendApplication", this::midEvent)
            .pageLabel("Request amended application - Make a Decision")
            .showCondition("coRefusalDecision=\"reject\" AND coGranted=\"No\"")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getRefusalRejectionAdditionalInfo)
            .done()
            .page("refusalDraft")
            .pageLabel("Refusal Draft")
            .showCondition("coGranted=\"No\" AND coRefusalDecision!=\"adminError\"")
            .complex(CaseData::getConditionalOrder)
            .readonlyWithLabel(ConditionalOrder::getRefusalOrderDocument, "View refusal order:")
            .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Legal advisor grant conditional order about to submit callback invoked. CaseID: {}", details.getId());

        final CaseData caseData = details.getData();
        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();

        caseData.getConditionalOrder().setIsAdminClarificationSubmitted(YesOrNo.NO);

        State endState;

        if (conditionalOrder.hasConditionalOrderBeenGranted()) {
            log.info("Legal advisor conditional order granted for case id: {}", details.getId());
            conditionalOrder.setDecisionDate(LocalDate.now(clock));
            endState = AwaitingPronouncement;

        } else if (REJECT.equals(conditionalOrder.getRefusalDecision())) {
            generateAndSetConditionalOrderRefusedDocument(
                caseData,
                details.getId(),
                REJECT
            );
            notificationDispatcher.send(rejectedNotification, caseData, details.getId());
            endState = AwaitingAmendedApplication;

        } else if (MORE_INFO.equals(conditionalOrder.getRefusalDecision())) {

            generateAndSetConditionalOrderRefusedDocument(
                caseData,
                details.getId(),
                MORE_INFO
            );

            notificationDispatcher.send(moreInfoDecisionNotification, caseData, details.getId());
            endState = AwaitingClarification;

        } else {
            endState = AwaitingAdminClarification;
        }

        conditionalOrder.setLegalAdvisorDecisions(
            conditionalOrder.addAuditRecord(
                conditionalOrder.getLegalAdvisorDecisions(),
                conditionalOrder.populateLegalAdvisorDecision(LocalDate.now(clock))
            )
        );

        caseData.getConditionalOrder().resetClarificationFields();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(endState)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                  final CaseDetails<CaseData, State> detailsBefore) {
        CaseData caseData = details.getData();

        caseData.getConditionalOrder().setRefusalOrderDocument(generateRefusalDocument(
            caseData,
            details.getId(),
            caseData.getConditionalOrder().getRefusalDecision()));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void generateAndSetConditionalOrderRefusedDocument(final CaseData caseData, final Long caseId, RefusalOption refusalOption) {

        Document refusalOrderDocument = caseData.getConditionalOrder().getRefusalOrderDocument();

        if (refusalOrderDocument == null) {
            refusalOrderDocument = generateRefusalDocument(caseData, caseId, refusalOption);
            caseData.getConditionalOrder().setRefusalOrderDocument(refusalOrderDocument);
        }

        caseData.getDocuments().setDocumentsGenerated(addDocumentToTop(
            caseData.getDocuments().getDocumentsGenerated(),
            DivorceDocument
                .builder()
                .documentLink(refusalOrderDocument)
                .documentFileName(refusalOrderDocument.getFilename())
                .documentType(CONDITIONAL_ORDER_REFUSAL)
                .build()
        ));
    }

    private Document generateRefusalDocument(final CaseData caseData, final Long caseId, RefusalOption refusalOption) {

        String templateId;
        Map<String, Object> templateContents;

        if (MORE_INFO.equals(refusalOption)) {
            templateId = CLARIFICATION_REFUSAL_ORDER_TEMPLATE_ID;
            templateContents = conditionalOrderRefusedForClarificationContent.apply(caseData, caseId);
        } else {
            templateId = REJECTED_REFUSAL_ORDER_TEMPLATE_ID;
            templateContents = conditionalOrderRefusedForAmendmentContent.apply(caseData, caseId);
        }

        log.info("Generating conditional order refusal document for templateId : {} caseId: {}", templateId, caseId);

        return caseDataDocumentService.renderDocument(
            templateContents,
            caseId,
            templateId,
            caseData.getApplicant1().getLanguagePreference(),
            REFUSAL_ORDER_DOCUMENT_NAME
        );
    }
}

