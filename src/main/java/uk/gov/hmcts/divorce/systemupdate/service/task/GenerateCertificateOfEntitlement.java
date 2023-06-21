package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CertificateOfEntitlementContent;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
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
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.BEFORE_DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_FO_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class GenerateCertificateOfEntitlement implements CaseTask {

    public static final String GET_A_DIVORCE = "get a divorce";
    public static final String END_YOUR_CIVIL_PARTNERSHIP = "end your civil partnership";
    public static final String IS_JOINT = "isJoint";
    public static final String IS_RESPONDENT = "isRespondent";

    @Value("${final_order.eligible_from_offset_days}")
    private long finalOrderOffsetDays;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private CertificateOfEntitlementContent certificateOfEntitlementContent;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    @Autowired
    private CommonContent commonContent;

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
            templateVars(caseData, caseId, caseData.getApplicant1(), caseData.getApplicant2()),
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

        Map<String, Object> templateVars = templateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1());

        boolean isJudicialSeparation = caseData.isJudicialSeparationCase();

        if (isJudicialSeparation) {
            templateVars.put(IS_RESPONDENT, true);
        } else {
            templateVars.put(
                PARTNER,
                commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference())
            );
        }

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2,
            templateVars,
            caseId,
            isJudicialSeparation
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
            templateVars(caseData, caseId, caseData.getApplicant2(), caseData.getApplicant1()),
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
            getSolicitorTemplateContent(caseData, caseId, true, caseData.getApplicant1().getLanguagePreference()),
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
            getSolicitorTemplateContent(caseData, caseId, false, caseData.getApplicant2().getLanguagePreference()),
            caseId,
            CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            caseData.getApplicant2().getLanguagePreference(),
            formatDocumentName(caseId, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    private Map<String, Object> templateVars(final CaseData caseData,
                                             final Long caseId,
                                             final Applicant applicant,
                                             final Applicant partner) {

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference());

        templateContent.put(NAME, applicant.isRepresented()
            ? applicant.getSolicitor().getName()
            : join(" ", applicant.getFirstName(), applicant.getLastName())
        );
        templateContent.put(ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, formatId(caseId));

        templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, caseData.isDivorce() ? GET_A_DIVORCE :  END_YOUR_CIVIL_PARTNERSHIP);
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, caseData.isDivorce() ? MARRIAGE : CIVIL_PARTNERSHIP);

        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        final LocalDateTime dateAndTimeOfHearing = conditionalOrder.getDateAndTimeOfHearing();
        final String dateOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(DATE_TIME_FORMATTER) : null;
        final String timeOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(TIME_FORMATTER) : null;
        final String beforeDateOfHearing = nonNull(dateAndTimeOfHearing)
            ? dateAndTimeOfHearing.minusDays(7).format(DATE_TIME_FORMATTER) : null;

        templateContent.put(COURT_NAME, conditionalOrder.getCourt() != null ? conditionalOrder.getCourt().getLabel() : null);
        templateContent.put(DATE_OF_HEARING, dateOfHearing);
        templateContent.put(TIME_OF_HEARING, timeOfHearing);
        if (nonNull(dateAndTimeOfHearing)) {
            templateContent.put(DATE_FO_ELIGIBLE_FROM, dateAndTimeOfHearing.plusDays(finalOrderOffsetDays).format(DATE_TIME_FORMATTER));
        } else {
            templateContent.put(DATE_FO_ELIGIBLE_FROM, null);
        }

        templateContent.put(BEFORE_DATE_OF_HEARING, beforeDateOfHearing);

        if (caseData.isJudicialSeparationCase()) {
            templateContent.put(IS_DIVORCE, caseData.isDivorce());
            templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());
            templateContent.put(
                PARTNER,
                commonContent.getPartner(caseData, partner, applicant.getLanguagePreference())
            );
        }

        return templateContent;
    }

    private Map<String, Object> getSolicitorTemplateContent(final CaseData caseData, final Long caseId, final boolean isApplicantSolicitor,
                                                            final LanguagePreference languagePreference) {
        Map<String, Object> templateContent = docmosisCommonContent.getBasicSolicitorTemplateContent(
            caseData, caseId, isApplicantSolicitor, languagePreference);

        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        final LocalDateTime dateAndTimeOfHearing = conditionalOrder.getDateAndTimeOfHearing();
        final String dateOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(DATE_TIME_FORMATTER) : null;
        final String timeOfHearing = nonNull(dateAndTimeOfHearing) ? dateAndTimeOfHearing.format(TIME_FORMATTER) : null;

        templateContent.put(COURT_NAME, conditionalOrder.getCourt() != null ? conditionalOrder.getCourt().getLabel() : null);
        templateContent.put(DATE_OF_HEARING, dateOfHearing);
        templateContent.put(TIME_OF_HEARING, timeOfHearing);

        return templateContent;
    }
}
