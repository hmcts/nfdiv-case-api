package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.PAPER_APPLICATION_RECEIVED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaperApplicationReceivedTemplateContent implements TemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;
    private final Clock clock;

    public static final String DATE_OF_RESPONSE = "dateOfResponse";
    private static final int SUBMISSION_RESPONSE_DAYS = 28;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(
            PAPER_APPLICATION_RECEIVED_TEMPLATE_ID
        );
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        Map<String, Object> templateContent = docmosisCommonContent
                .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(RECIPIENT_NAME,  applicant.getFullName());
        templateContent.put(RECIPIENT_ADDRESS,  AddressUtil.getPostalAddress(applicant.getAddress()));
        templateContent.put(IS_DIVORCE, caseData.isDivorce() ? CommonContent.YES : NO);
        templateContent.put(DATE_OF_RESPONSE, LocalDate.now(clock)
            .plusDays(SUBMISSION_RESPONSE_DAYS).format(DATE_TIME_FORMATTER));

        return templateContent;
    }
}
