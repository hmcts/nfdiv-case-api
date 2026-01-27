package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.common.notification.Applicant2AppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.common.service.task.ProgressApplicant1FinalOrderState;
import uk.gov.hmcts.divorce.common.service.task.ProgressApplicant2FinalOrderState;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant1;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant2;
import uk.gov.hmcts.divorce.common.service.task.SetFinalOrderFieldsAsApplicant2Sol;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.content.templatecontent.RespondentFinalOrderAnswersTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_FINAL_ORDER_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_FINAL_ORDER_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_FINAL_ORDER_ANSWERS;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplyForFinalOrderService {

    private static final String APP1_ALREADY_APPLIED_FOR_FO_ERR_MESSAGE = "Applicant / Applicant 1 has already applied for final order.";
    private static final String APP2_ALREADY_APPLIED_FOR_FO_ERR_MESSAGE = "Applicant 2 has already applied for final order.";

    private final SetFinalOrderFieldsAsApplicant1 setFinalOrderFieldsAsApplicant1;

    private final SetFinalOrderFieldsAsApplicant2 setFinalOrderFieldsAsApplicant2;

    private final SetFinalOrderFieldsAsApplicant2Sol setFinalOrderFieldsAsApplicant2Sol;

    private final ProgressApplicant1FinalOrderState progressApplicant1FinalOrderState;

    private final ProgressApplicant2FinalOrderState progressApplicant2FinalOrderState;

    private final Applicant2AppliedForFinalOrderNotification applicant2AppliedForFinalOrderNotification;

    private final NotificationDispatcher notificationDispatcher;

    private final DocumentGenerator documentGenerator;

    private final CaseDataDocumentService caseDataDocumentService;

    private final RespondentFinalOrderAnswersTemplateContent respondentFinalOrderAnswersTemplateContent;

    private final Clock clock;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CaseDetails<CaseData, State> applyForFinalOrderAsApplicant1(final CaseDetails<CaseData, State> caseDetails) {

        return CaseTaskRunner.caseTasks(
            setFinalOrderFieldsAsApplicant1,
            progressApplicant1FinalOrderState
        ).run(caseDetails);
    }

    public CaseDetails<CaseData, State> applyForFinalOrderAsApplicant2(final CaseDetails<CaseData, State> caseDetails) {

        return CaseTaskRunner.caseTasks(
            setFinalOrderFieldsAsApplicant2,
            progressApplicant2FinalOrderState
        ).run(caseDetails);
    }

    public CaseDetails<CaseData, State> applyForFinalOrderAsApplicant2Sol(final CaseDetails<CaseData, State> caseDetails) {

        return CaseTaskRunner.caseTasks(
            setFinalOrderFieldsAsApplicant2Sol,
            progressApplicant2FinalOrderState
        ).run(caseDetails);
    }

    public List<String> validateApplyForFinalOrder(final CaseData caseData, boolean isApplicant2Event) {
        final var finalOrder = caseData.getFinalOrder();
        final List<String> errors = new ArrayList<>();

        if (YES.equals(finalOrder.getApplicant1AppliedForFinalOrderFirst()) && !isApplicant2Event) {
            errors.add(APP1_ALREADY_APPLIED_FOR_FO_ERR_MESSAGE);
        }

        if (YES.equals(finalOrder.getApplicant2AppliedForFinalOrderFirst()) && isApplicant2Event) {
            errors.add(APP2_ALREADY_APPLIED_FOR_FO_ERR_MESSAGE);
        }

        return errors;
    }

    public void sendRespondentAppliedForFinalOrderNotifications(final CaseDetails<CaseData, State> caseDetails) {
        log.info("Sending Respondent Applied For Final Order Notification for Case Id: {}", caseDetails.getId());

        notificationDispatcher.send(applicant2AppliedForFinalOrderNotification, caseDetails.getData(), caseDetails.getId());
    }

    public void generateAndStoreFinalOrderAnswersDocument(final CaseData caseData, final Long caseId) {
        log.info("Generating Final Order Answers document for Case Id: {}", caseId);

        Document document = caseDataDocumentService.renderDocument(
            respondentFinalOrderAnswersTemplateContent.getTemplateContent(caseData, caseId, caseData.getApplicant2()),
            caseId,
            RESPONDENT_FINAL_ORDER_ANSWERS_TEMPLATE_ID,
            caseData.getApplicant2().getLanguagePreference(),
            RESPONDENT_FINAL_ORDER_ANSWERS_DOCUMENT_NAME + LocalDateTime.now(clock).format(formatter)
        );

        DivorceDocument respondentFinalOrderAnswersDocument = DivorceDocument
            .builder()
            .documentType(RESPONDENT_FINAL_ORDER_ANSWERS)
            .documentLink(document)
            .build();

        caseData.getDocuments().setDocumentsGenerated(
            CaseDocuments.addDocumentToTop(caseData.getDocuments().getDocumentsGenerated(), respondentFinalOrderAnswersDocument));
    }
}
