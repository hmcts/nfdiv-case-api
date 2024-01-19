package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_REMINDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_JOINT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class ConditionalOrderReminderTemplateContent implements TemplateContent {

    public static final String DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION = "divorceOrEndCivilPartnershipApplication";
    public static final String DIVORCE_OR_END_CIVIL_PARTNERSHIP = "divorceOrEndCivilPartnership";
    public static final String APPLICANT_FIRST_NAME = "applicantFirstName";
    public static final String APPLICANT_LAST_NAME = "applicantLastName";
    public static final String APPLICANT_ADDRESS = "applicantAddress";

    public static final String DIVORCE = "get a divorce";
    public static final String END_THE_CIVIL_PARTNERSHIP = "end the civil partnership";


    @Override
    public Map<String, Object> getTemplateContent(final CaseData caseData, final Long caseId, Applicant applicant) {

        final Map<String, Object> templateContent = new HashMap<>();

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.put(APPLICANT_FIRST_NAME, applicant.getFirstName());
        templateContent.put(APPLICANT_LAST_NAME, applicant.getLastName());
        templateContent.put(APPLICANT_ADDRESS, applicant.getAddress());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, DIVORCE_APPLICATION);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, DIVORCE);
        } else {
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, END_THE_CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP_APPLICATION, TO_END_A_CIVIL_PARTNERSHIP);
        }

        return templateContent;
    }

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(CONDITIONAL_ORDER_REMINDER_TEMPLATE_ID);
    }
}
