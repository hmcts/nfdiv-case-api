package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.LanguagePreference;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.document.DocAssemblyService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.common.model.DocumentType.Petition;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_PETITION;

@Component
@Slf4j
public class MiniPetitionDraft implements CaseDataUpdater {

    @Autowired
    private DocAssemblyService docAssemblyService;

    @Autowired
    private DocmosisTemplatesConfig docmosisTemplatesConfig;

    @Override
    public CaseDataContext updateCaseData(CaseDataContext caseDataContext, CaseDataUpdaterChain caseDataUpdaterChain) {
        log.info("Executing handler for generating mini draft for case id {} ", caseDataContext.getCaseId());

        CaseData caseData = caseDataContext.copyOfCaseData();

        LanguagePreference languagePreference;

        if (caseData.getLanguagePreferenceWelsh().equals(NO)) {
            languagePreference = ENGLISH;

        } else {
            languagePreference = WELSH;
        }

        String templateId = docmosisTemplatesConfig.getTemplates().get(languagePreference).get(DIVORCE_MINI_PETITION);

        DocumentInfo documentInfo = docAssemblyService.generateAndStoreDraftPetition(
            caseData,
            caseDataContext.getCaseId(),
            caseDataContext.getUserAuthToken(),
            templateId
        );

        Document ccdDocument = new Document(
            documentInfo.getUrl(),
            documentInfo.getFilename(),
            documentInfo.getBinaryUrl()
        );

        DivorceDocument divorceDocument = DivorceDocument
            .builder()
            .documentLink(ccdDocument)
            .documentType(Petition)
            .build();

        CaseData updatedCaseData = caseData
            .toBuilder()
            .documentsGenerated(singletonList(divorceDocument))
            .build();

        return caseDataUpdaterChain.processNext(caseDataContext.handlerContextWith(updatedCaseData));
    }
}
