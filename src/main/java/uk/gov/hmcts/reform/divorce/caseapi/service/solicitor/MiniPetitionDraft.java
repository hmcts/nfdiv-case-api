package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.divorce.caseapi.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.reform.divorce.caseapi.model.DocumentInfo;
import uk.gov.hmcts.reform.divorce.caseapi.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.caseapi.service.DocAssemblyService;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataContext;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataUpdater;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataUpdaterChain;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.DivorceDocument;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.reform.divorce.caseapi.constants.DocumentConstants.DIVORCE_MINI_PETITION;
import static uk.gov.hmcts.reform.divorce.caseapi.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.caseapi.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.reform.divorce.ccd.model.enums.DocumentType.Petition;

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
