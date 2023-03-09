package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DivorceApplicationJointTemplateContent;
import uk.gov.hmcts.divorce.document.content.ApplicationSoleTemplateContent;

import java.time.Clock;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_JOINT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JUDICIAL_SEPARATION_SOLE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;

@Component
@Slf4j
public class GenerateApplication implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private ApplicationSoleTemplateContent applicationSoleTemplateContent;

    @Autowired
    private DivorceApplicationJointTemplateContent divorceApplicationJointTemplateContent;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        if (caseData.isJudicialSeparationCase()) {
            log.info("Executing handler for generating judicial separation for case id {} ", caseId);
        } else {
            log.info("Executing handler for generating divorce application for case id {} ", caseId);
        }

        final Map<String, Object> templateContent;
        final String templateId;
        final String documentName;
        LanguagePreference languagePreference = ENGLISH;
        var isJudicialSeparationCase = caseData.isJudicialSeparationCase();

        if (caseData.getApplicationType().isSole()) {
            templateContent = applicationSoleTemplateContent.apply(caseData, caseId);
            templateId = isJudicialSeparationCase ? NFD_NOP_JUDICIAL_SEPARATION_SOLE_TEMPLATE_ID : DIVORCE_APPLICATION_SOLE;
            documentName = isJudicialSeparationCase ? JUDICIAL_SEPARATION_APPLICATION_DOCUMENT_NAME : DIVORCE_APPLICATION_DOCUMENT_NAME;
        } else {
            templateContent = divorceApplicationJointTemplateContent.apply(caseData, caseId);
            templateId = DIVORCE_APPLICATION_JOINT;

            // This will be changed when we do joint template ticket to get correct document name based on JS
            documentName = DIVORCE_APPLICATION_DOCUMENT_NAME;
            if (YES.equals(caseData.getApplicant1().getLanguagePreferenceWelsh())
                && YES.equals(caseData.getApplicant2().getLanguagePreferenceWelsh())) {
                languagePreference = WELSH;
            }
        }

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            APPLICATION,
            templateContent,
            caseId,
            templateId,
            languagePreference,
            formatDocumentName(caseId, documentName, now(clock))
        );

        return caseDetails;
    }
}
