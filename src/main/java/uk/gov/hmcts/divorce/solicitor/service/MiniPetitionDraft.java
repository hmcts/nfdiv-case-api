package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.ccd.model.CaseData;
import uk.gov.hmcts.divorce.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.document.DocAssemblyService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.divorce.model.LanguagePreference;
import uk.gov.hmcts.divorce.util.CaseDataContext;
import uk.gov.hmcts.divorce.util.CaseDataUpdater;
import uk.gov.hmcts.divorce.util.CaseDataUpdaterChain;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.ccd.model.enums.DocumentType.Petition;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_PETITION;
import static uk.gov.hmcts.divorce.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.model.LanguagePreference.WELSH;

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
