package uk.gov.hmcts.divorce.caseworker.service.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.RespondentSolicitorAosInvitationTemplateContent;

import java.util.Map;
import java.util.function.Supplier;

import static uk.gov.hmcts.divorce.divorcecase.util.AccessCodeGenerator.generateAccessCode;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_AOS_INVITATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESP_SOLICITOR_AOS_INVITATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DOCUMENT_TYPE_RESPONDENT_INVITATION;

@Component
@Slf4j
public class RespondentSolicitorAosInvitation implements CaseDataUpdater {
    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    //TODO: Use correct template content when application template requirements are known.
    @Autowired
    private RespondentSolicitorAosInvitationTemplateContent templateContent;

    @Override
    public CaseDataContext updateCaseData(final CaseDataContext caseDataContext,
                                          final CaseDataUpdaterChain caseDataUpdaterChain) {

        log.info("Executing handler for generating respondent aos invitation for case id {} ", caseDataContext.getCaseId());

        final CaseData caseData = caseDataContext.copyOfCaseData();
        final Long caseId = caseDataContext.getCaseId();
        final String userAuthToken = caseDataContext.getUserAuthToken();

        final Supplier<Map<String, Object>> templateContentSupplier = templateContent
            .apply(caseDataContext.copyOfCaseData(), caseId, caseDataContext.getCreatedDate());

        caseData.getCaseInvite().setAccessCode(generateAccessCode());

        final CaseData updatedCaseData = caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            DOCUMENT_TYPE_RESPONDENT_INVITATION,
            templateContentSupplier,
            caseId,
            userAuthToken,
            RESP_SOLICITOR_AOS_INVITATION,
            RESP_AOS_INVITATION_DOCUMENT_NAME,
            caseData.getApplicant1().getLanguagePreference()
        );

        return caseDataUpdaterChain.processNext(caseDataContext.handlerContextWith(updatedCaseData));
    }
}
