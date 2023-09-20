package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_CAN_APPLY_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_CAN_APPLY_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_CAN_APPLY_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_CAN_APPLY_APP2;
import static uk.gov.hmcts.divorce.notification.CommonContent.FINAL_ORDER_OVERDUE_DATE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
public class GenerateApplyForFinalOrderDocument {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    public void generateApplyForFinalOrder(final CaseData caseData,
                                           final Long caseId,
                                           final Applicant applicant,
                                           final Applicant partner,
                                           final boolean isApplicant1) {

        log.info("Generating apply for final order pdf for CaseID: {}", caseId);

        LocalDateTime now = LocalDateTime.now(clock);

        DocumentType finalOrderDocType = isApplicant1 ? FINAL_ORDER_CAN_APPLY_APP1 : FINAL_ORDER_CAN_APPLY_APP2;
        final Map<String, Object> templateVars =
            commonContent.templateContentCanApplyForCoOrFo(caseData, caseId, applicant, partner, now.toLocalDate());
        templateVars.put(FINAL_ORDER_OVERDUE_DATE, caseData.getFinalOrder().getDateFinalOrderEligibleFrom().plusMonths(12)
            .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            finalOrderDocType,
            templateVars,
            caseId,
            FINAL_ORDER_CAN_APPLY_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, FINAL_ORDER_CAN_APPLY_DOCUMENT_NAME, LocalDateTime.now(clock))
        );

        log.info("Completed generating apply for final order pdf for CaseID: {}", caseId);
    }
}
