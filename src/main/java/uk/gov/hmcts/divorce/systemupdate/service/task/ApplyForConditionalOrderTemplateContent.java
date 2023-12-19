package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_CAN_APPLY_TEMPLATE_ID;

@Component
@Slf4j
public class ApplyForConditionalOrderTemplateContent implements TemplateContent {

    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    @Autowired
    private Clock clock;

    public Map<String, Object> generateApplyForConditionalOrder(final CaseData caseData,
                                                                final Long caseId,
                                                                final Applicant applicant,
                                                                final Applicant partner) {

        log.info("Generating apply for conditional order pdf for CaseID: {}", caseId);

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
                applicant.getLanguagePreference());

        LocalDateTime now = LocalDateTime.now(clock);

        templateContent.putAll(commonContent.templateContentCanApplyForCoOrFo(caseData, caseId, applicant, partner, now.toLocalDate()));

        return templateContent;
    }

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(CONDITIONAL_ORDER_CAN_APPLY_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        final Applicant partner = applicant.equals(caseData.getApplicant1()) ? caseData.getApplicant2() : caseData.getApplicant1();
        return generateApplyForConditionalOrder(caseData, caseId, applicant, partner);
    }
}
