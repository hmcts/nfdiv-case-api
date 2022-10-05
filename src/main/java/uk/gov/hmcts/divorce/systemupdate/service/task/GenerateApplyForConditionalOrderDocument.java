package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_CAN_APPLY_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_CAN_APPLY_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_CAN_APPLY;

@Component
@Slf4j
public class GenerateApplyForConditionalOrderDocument {

    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    @Autowired
    private Clock clock;

    public void generateApplyForConditionalOrder(final CaseData caseData,
                                                 final Long caseId,
                                                 final Applicant applicant,
                                                 final Applicant partner) {

        log.info("Generating apply for conditional order pdf for CaseID: {}", caseId);

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
                applicant.getLanguagePreference());

        LocalDateTime now = LocalDateTime.now(clock);

        templateContent.putAll(commonContent.templateContentCanApplyForCoOrFo(caseData, caseId, applicant, partner, now.toLocalDate()));

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_CAN_APPLY,
            templateContent,
            caseId,
            CONDITIONAL_ORDER_CAN_APPLY_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, CONDITIONAL_ORDER_CAN_APPLY_DOCUMENT_NAME, now)
        );

        log.info("Completed generating apply for conditional order pdf for CaseID: {}", caseId);
    }
}
