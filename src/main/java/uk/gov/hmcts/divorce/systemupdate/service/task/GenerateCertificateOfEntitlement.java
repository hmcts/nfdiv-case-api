package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CertificateOfEntitlementContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;

@Component
@Slf4j
public class GenerateCertificateOfEntitlement implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private CertificateOfEntitlementContent certificateOfEntitlementContent;
    @Autowired
    private GenerateCertificateOfEntitlementHelper generateCertificateOfEntitlementHelper;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        log.info("Generating certificate of entitlement pdf for CaseID: {}", caseDetails.getId());

        final Document certificateOfEntitlement = caseDataDocumentService.renderDocument(
            certificateOfEntitlementContent.apply(caseData, caseId),
            caseId,
            caseData.isJudicialSeparationCase() ? CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID
            : CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID,
            caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(caseId, CERTIFICATE_OF_ENTITLEMENT_NAME, now(clock))
        );

        final DivorceDocument coeDivorceDocument = DivorceDocument
            .builder()
            .documentLink(certificateOfEntitlement)
            .documentFileName(certificateOfEntitlement.getFilename())
            .documentType(CERTIFICATE_OF_ENTITLEMENT)
            .build();

        caseData.getConditionalOrder().setCertificateOfEntitlementDocument(coeDivorceDocument);

        log.info("Completed generating certificate of entitlement pdf for CaseID: {}", caseDetails.getId());

        return caseDetails;
    }

    public void generateCertificateOfEntitlementCoverLetters(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final boolean isJudicialSeparation = caseData.isJudicialSeparationCase();

        if (caseData.getApplicant1().isApplicantOffline()) {
            if (caseData.getApplicant1().isRepresented() && isJudicialSeparation) {
                generateApplicant1SolicitorCertificateOfEntitlementCoverLetter(caseData, caseId);
            } else {
                generateApplicant1CertificateOfEntitlementCoverLetter(caseData, caseId);
            }
        }

        if (isBlank(caseData.getApplicant2EmailAddress()) || caseData.getApplicant2().isApplicantOffline()) {

            if (caseData.getApplicant2().isRepresented() && isJudicialSeparation) {
                generateApplicant2SolicitorCertificateOfEntitlementCoverLetter(caseData, caseId);
            } else {
                if (caseData.getApplicationType().isSole()) {
                    generateRespondentCertificateOfEntitlementCoverLetter(caseData, caseId);
                } else {
                    generateApplicant2CertificateOfEntitlementCoverLetter(caseData, caseId);
                }
            }
        }

    }

    public void removeExistingAndGenerateNewCertificateOfEntitlementCoverLetters(CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();
        final List<DocumentType> documentTypesToRemove =
            List.of(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2);

        if (!isEmpty(caseData.getDocuments().getDocumentsGenerated())) {
            caseData.getDocuments().getDocumentsGenerated()
                .removeIf(document -> documentTypesToRemove.contains(document.getValue().getDocumentType()));
        }

        generateCertificateOfEntitlementCoverLetters(caseDetails);
    }

    private void generateApplicant1CertificateOfEntitlementCoverLetter(final CaseData caseData,
                                                                        final Long caseId) {
        log.info("Generating certificate of entitlement cover letter for Applicant / Applicant 1 for case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            generateCertificateOfEntitlementHelper.getTemplateContent(caseData, caseId, caseData.getApplicant1()),
            caseId,
            caseData.isJudicialSeparationCase()
                ? CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID
                : CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(caseId, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    private void generateRespondentCertificateOfEntitlementCoverLetter(final CaseData caseData,
                                                                       final Long caseId) {
        log.info("Generating certificate of entitlement cover letter for Respondent for case id {} ", caseId);

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            generateCertificateOfEntitlementHelper.getRespondentTemplateContent(caseData, caseId),
            caseId,
            caseData.isJudicialSeparationCase()
                ? CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID
                : CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID,
            caseData.getApplicant2().getLanguagePreference(),
            formatDocumentName(caseId, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    private void generateApplicant2CertificateOfEntitlementCoverLetter(final CaseData caseData,
                                                                       final Long caseId) {
        log.info("Generating certificate of entitlement cover letter for Applicant 2 for case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            generateCertificateOfEntitlementHelper.getTemplateContent(caseData, caseId, caseData.getApplicant2()),
            caseId,
            caseData.isJudicialSeparationCase()
                ? CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID
                : CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant2().getLanguagePreference(),
            formatDocumentName(caseId, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    private void generateApplicant1SolicitorCertificateOfEntitlementCoverLetter(final CaseData caseData,
                                                                                final Long caseId) {
        log.info("Generating certificate of entitlement cover letter for Applicant / Applicant 1 solicitor for case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1,
            generateCertificateOfEntitlementHelper.getSolicitorTemplateContent(
                caseData, caseId, true, caseData.getApplicant1().getLanguagePreference()),
            caseId,
            CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(caseId, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    private void generateApplicant2SolicitorCertificateOfEntitlementCoverLetter(final CaseData caseData,
                                                                                final Long caseId) {
        log.info("Generating certificate of entitlement cover letter for Respondent / Applicant 2 solicitor for case id {} ", caseId);
        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            generateCertificateOfEntitlementHelper.getSolicitorTemplateContent(
                caseData, caseId, false, caseData.getApplicant2().getLanguagePreference()),
            caseId,
            CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant2().getLanguagePreference(),
            formatDocumentName(caseId, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }
}
