package uk.gov.hmcts.divorce.legaladvisor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusalContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.legaladvisor.notification.LegalAdvisorMoreInfoDecisionNotification;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.ADMIN_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAdminClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAmendedApplication;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ClarificationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REFUSAL_ORDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REFUSAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;

@Component
@Slf4j
public class LegalAdvisorMakeDecision implements CCDConfig<CaseData, State, UserRole> {

    public static final String LEGAL_ADVISOR_MAKE_DECISION = "legal-advisor-make-decision";

    @Autowired
    private LegalAdvisorMoreInfoDecisionNotification notification;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private ConditionalOrderRefusalContent conditionalOrderRefusalContent;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(LEGAL_ADVISOR_MAKE_DECISION)
            .forStates(AwaitingLegalAdvisorReferral, ClarificationSubmitted)
            .name("Make a decision")
            .description("Grant Conditional Order")
            .endButtonLabel("Submit")
            .aboutToStartCallback(this::aboutToStart)
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, LEGAL_ADVISOR)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                APPLICANT_1_SOLICITOR))
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
            .page("refusalOrderClarification")
            .pageLabel("Refusal Order:Clarify - Make a Decision")
            .showCondition("coRefusalDecision=\"moreInfo\" AND coGranted=\"No\"")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getRefusalClarificationReason)
                .mandatory(ConditionalOrder::getRefusalClarificationAdditionalInfo)
            .done()
            .page("adminErrorClarification")
            .pageLabel("Admin error - Make a Decision")
            .showCondition("coRefusalDecision=\"adminError\" AND coGranted=\"No\"")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getRefusalAdminErrorInfo)
            .done()
            .page("amendApplication")
            .pageLabel("Request amended application - Make a Decision")
            .showCondition("coRefusalDecision=\"reject\" AND coGranted=\"No\"")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getRefusalRejectionReason)
                .mandatory(ConditionalOrder::getRefusalRejectionAdditionalInfo,
                "coRefusalRejectionReasonCONTAINS \"other\" "
                    + "OR coRefusalRejectionReasonCONTAINS \"noCriteria\" " // added for backward compatibility
                    + "OR coRefusalRejectionReasonCONTAINS \"insufficentDetails\"") // added for backward compatibility
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        CaseData caseData = details.getData();
        caseData.getConditionalOrder().resetRefusalFields();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder().data(caseData).build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Legal advisor grant conditional order about to submit callback invoked. CaseID: {}", details.getId());

        final CaseData caseData = details.getData();
        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();

        State endState;

        if (conditionalOrder.hasConditionalOrderBeenGranted()) {
            log.info("Legal advisor conditional order granted for case id: {}", details.getId());
            conditionalOrder.setDecisionDate(LocalDate.now(clock));
            endState = AwaitingPronouncement;

        } else if (ADMIN_ERROR.equals(conditionalOrder.getRefusalDecision())) {
            endState = AwaitingAdminClarification;
        } else if (MORE_INFO.equals(conditionalOrder.getRefusalDecision())) {
            notificationDispatcher.send(notification, caseData, details.getId());
            endState = AwaitingClarification;
        } else {
            generateAndSetConditionalOrderRefusedDocument(
                caseData,
                details.getId()
            );
            endState = AwaitingAmendedApplication;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(endState)
            .build();
    }

    private void generateAndSetConditionalOrderRefusedDocument(final CaseData caseData,
                                                               final Long caseId) {

        log.info("Generating conditional order refused document for templateId : {} caseId: {}",
            REFUSAL_ORDER_TEMPLATE_ID, caseId);

        var templateContents = conditionalOrderRefusalContent.apply(caseData, caseId);

        Document document = caseDataDocumentService.renderDocument(
            templateContents,
            caseId,
            REFUSAL_ORDER_TEMPLATE_ID,
            ENGLISH,
            REFUSAL_ORDER_DOCUMENT_NAME
        );

        var refusalConditionalOrderDoc = DivorceDocument
            .builder()
            .documentLink(document)
            .documentFileName(document.getFilename())
            .documentType(CONDITIONAL_ORDER_REFUSAL)
            .build();

        caseData.getDocuments().addToDocumentsGenerated(
            ListValue
                .<DivorceDocument>builder()
                .id(UUID.randomUUID().toString())
                .value(refusalConditionalOrderDoc)
                .build()
        );
    }
}

