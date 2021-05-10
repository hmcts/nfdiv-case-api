package uk.gov.hmcts.divorce.solicitor.service.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
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
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;

@Component
@Slf4j
public class MiniApplicationDraft implements CaseDataUpdater {

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

        String templateName = docmosisTemplatesConfig.getTemplates().get(languagePreference).get(DIVORCE_MINI_APPLICATION);

        DocumentInfo documentInfo = docAssemblyService.renderDocument(
            caseData,
            caseDataContext.getCaseId(),
            caseDataContext.getCreatedDate(),
            caseDataContext.getUserAuthToken(),
            templateName
        );

        Document ccdDocument = new Document(
            documentInfo.getUrl(),
            documentInfo.getFilename(),
            documentInfo.getBinaryUrl()
        );

        DivorceDocument divorceDocument = DivorceDocument
            .builder()
            .documentLink(ccdDocument)
            .documentFileName(documentInfo.getFilename())
            .documentType(APPLICATION)
            .build();


        ListValue<DivorceDocument> value = ListValue
            .<DivorceDocument>builder()
            .id(APPLICATION.getLabel())
            .value(divorceDocument)
            .build();

        CaseData updatedCaseData = caseData
            .toBuilder()
            .documentsGenerated(singletonList(value))
            .build();

        return caseDataUpdaterChain.processNext(caseDataContext.handlerContextWith(updatedCaseData));
    }
}
