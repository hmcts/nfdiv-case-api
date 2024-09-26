package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.document.DocumentConstants.APPLIED_FOR_CONDITIONAL_ORDER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppliedForCoTemplateContent implements TemplateContent {

    private final Clock clock;
    private static final String DATE_D84_RECEIVED = "dateD84Received";
    private static final String GRANTED_DATE = "grantedDate";


    @Override
    public List<String> getSupportedTemplates() {
        return List.of(APPLIED_FOR_CONDITIONAL_ORDER_LETTER_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(final CaseData caseData,
                                                final Long caseId,
                                                final Applicant applicant) {
        final Map<String, Object> templateContent = new HashMap<>();

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(NAME, join(" ", applicant.getFirstName(), applicant.getLastName()));
        templateContent.put(ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());
        templateContent.put(DATE, now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(DATE_D84_RECEIVED, caseData.getConditionalOrder().getDateD84FormScanned().format(DATE_TIME_FORMATTER));
        templateContent.put(GRANTED_DATE, now(clock).plusWeeks(4).format(DATE_TIME_FORMATTER));

        return templateContent;
    }
}
