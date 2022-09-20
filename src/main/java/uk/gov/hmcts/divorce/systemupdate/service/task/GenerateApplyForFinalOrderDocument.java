package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDateTime;

import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_CAN_APPLY_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_CAN_APPLY_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_CAN_APPLY;

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
                                                 final Applicant partner) {

        log.info("Generating apply for final order pdf for CaseID: {}", caseId);

        LocalDateTime now = LocalDateTime.now(clock);

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            FINAL_ORDER_CAN_APPLY,
            commonContent.templateContentCanApplyForCoOrFo(caseData, caseId, applicant, partner, now.toLocalDate()),
            caseId,
            FINAL_ORDER_CAN_APPLY_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, FINAL_ORDER_CAN_APPLY_DOCUMENT_NAME, LocalDateTime.now(clock))
        );

        log.info("Completed generating apply for final order pdf for CaseID: {}", caseId);
    }
}
