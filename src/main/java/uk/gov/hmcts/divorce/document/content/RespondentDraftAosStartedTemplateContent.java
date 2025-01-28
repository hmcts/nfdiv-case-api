package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_DRAFT_AOS_STARTED_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class RespondentDraftAosStartedTemplateContent implements TemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;
    private final CommonContent commonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(
                RESPONDENT_DRAFT_AOS_STARTED_APPLICATION_TEMPLATE_ID
        );
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {

        Map<String, Object> templateContent = docmosisCommonContent
                .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", caseId, caseData.getDivorceOrDissolution());

        templateContent.put(NAME, applicant.getFullName());
        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(ADDRESS, AddressUtil.getPostalAddress(applicant.getAddress()));
        templateContent.put(IS_DIVORCE,  caseData.isDivorce());
        templateContent.put(PARTNER,  commonContent.getPartner(caseData, applicant, applicant.getLanguagePreference()));

        return templateContent;
    }
}
