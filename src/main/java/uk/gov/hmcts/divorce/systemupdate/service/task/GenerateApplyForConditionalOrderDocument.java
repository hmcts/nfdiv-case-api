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
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_CAN_APPLY_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_CAN_APPLY_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_CAN_APPLY;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class GenerateApplyForConditionalOrderDocument {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    public void generateApplyForConditionalOrder(final CaseData caseData,
                                                 final Long caseId,
                                                 final Applicant applicant,
                                                 final Applicant partner) {

        log.info("Generating apply for conditional order pdf for CaseID: {}", caseId);

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_CAN_APPLY,
            templateContent(caseData, caseId, applicant, partner),
            caseId,
            CONDITIONAL_ORDER_CAN_APPLY_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, CONDITIONAL_ORDER_CAN_APPLY_DOCUMENT_NAME, LocalDateTime.now(clock))
        );

        log.info("Completed generating apply for conditional order pdf for CaseID: {}", caseId);
    }

    private Map<String, Object> templateContent(final CaseData caseData,
                                                final Long caseId,
                                                final Applicant applicant,
                                                final Applicant partner) {

        final Map<String, Object> templateContent = new HashMap<>();

        templateContent.put(CASE_REFERENCE, caseId != null ? formatId(caseId) : null);

        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(ADDRESS, applicant.getPostalAddress());
        templateContent.put(PARTNER, commonContent.getPartner(caseData, partner, applicant.getLanguagePreference()));

        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());
        templateContent.put(IS_DIVORCE, caseData.isDivorce());

        return templateContent;
    }
}
