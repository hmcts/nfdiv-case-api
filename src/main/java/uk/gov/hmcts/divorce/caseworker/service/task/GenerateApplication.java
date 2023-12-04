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
import uk.gov.hmcts.divorce.document.content.ApplicationJointContent;
import uk.gov.hmcts.divorce.document.content.ApplicationSoleContent;

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
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_JOINT_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_SOLE_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;

@Component
@Slf4j
public class GenerateApplication implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private ApplicationSoleContent applicationSoleTemplateContent;

    @Autowired
    private ApplicationJointContent applicationJointTemplateContent;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        var isJudicialSeparationCase = caseData.isJudicialSeparationCase();

        if (isJudicialSeparationCase) {
            log.info("Executing handler for generating judicial separation for case id {} ", caseId);
        } else {
            log.info("Executing handler for generating divorce application for case id {} ", caseId);
        }

        final Map<String, Object> templateContent;
        final String templateId;

        LanguagePreference languagePreference = ENGLISH;

        if (caseData.getApplicationType().isSole()) {
            templateContent = applicationSoleTemplateContent.apply(caseData, caseId);
            templateId = isJudicialSeparationCase ? JUDICIAL_SEPARATION_SOLE_APPLICATION_TEMPLATE_ID : DIVORCE_APPLICATION_SOLE;
        } else {
            templateContent = applicationJointTemplateContent.apply(caseData, caseId);
            templateId = isJudicialSeparationCase ? JUDICIAL_SEPARATION_JOINT_APPLICATION_TEMPLATE_ID : DIVORCE_APPLICATION_JOINT;

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
            formatDocumentName(caseId,
                    isJudicialSeparationCase ? JUDICIAL_SEPARATION_APPLICATION_DOCUMENT_NAME : DIVORCE_APPLICATION_DOCUMENT_NAME,
                    now(clock))
        );

        return caseDetails;
    }
}
