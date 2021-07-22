package uk.gov.hmcts.divorce.caseworker.service.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.MiniApplicationTemplateContent;

import java.util.Map;
import java.util.function.Supplier;

import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;

@Component
@Slf4j
public class GenerateMiniApplication implements CaseDataAction {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private MiniApplicationTemplateContent templateContent;

    @Override
    public CaseDataContext apply(final CaseDataContext caseDataContext) {

        log.info("Executing handler for generating mini application for case id {} ", caseDataContext.getCaseId());

        final CaseData caseData = caseDataContext.copyOfCaseData();
        final Long caseId = caseDataContext.getCaseId();
        final String userAuthToken = caseDataContext.getUserAuthToken();

        final Supplier<Map<String, Object>> templateContentSupplier = templateContent
            .apply(caseDataContext.copyOfCaseData(), caseId, caseDataContext.getCreatedDate());

        final CaseData updatedCaseData = caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            DIVORCE_APPLICATION,
            templateContentSupplier,
            caseId,
            userAuthToken,
            DIVORCE_MINI_APPLICATION,
            DIVORCE_MINI_APPLICATION_DOCUMENT_NAME,
            caseData.getApplicant1().getLanguagePreference()
        );

        return caseDataContext.handlerContextWith(updatedCaseData);
    }
}
