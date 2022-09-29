package uk.gov.hmcts.divorce.systemupdate.service.print;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.firstElement;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.BEFORE_DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_FO_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;


@Component
@Slf4j
public class CertificateOfEntitlementPrinter {

    public static final String LETTER_TYPE_CERTIFICATE_OF_ENTITLEMENT = "certificate-of-entitlement";
    public static final String NAME = "name";
    public static final String ADDRESS = "address";
    public static final String GET_A_DIVORCE = "get a divorce";
    public static final String END_YOUR_CIVIL_PARTNERSHIP = "end your civil partnership";

    private static final int EXPECTED_DOCUMENTS_SIZE = 2;

    @Autowired
    private BulkPrintService bulkPrintService;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private Clock clock;

    @Value("${final_order.eligible_from_offset_days}")
    private long finalOrderOffsetDays;

    @Value("${court.locations.serviceCentre.email}")
    private String email;

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    public void sendLetter(final CaseData caseData, final Long caseId, final Applicant applicant) {

        generateCoversheet(caseData, caseId, applicant);

        final List<Letter> certificateOfEntitlementLetters = certificateOfEntitlementLetters(caseData);

        if (!isEmpty(certificateOfEntitlementLetters) && certificateOfEntitlementLetters.size() == EXPECTED_DOCUMENTS_SIZE) {
            final String caseIdString = caseId.toString();
            final Print print =
                new Print(certificateOfEntitlementLetters, caseIdString, caseIdString, LETTER_TYPE_CERTIFICATE_OF_ENTITLEMENT);
            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
        } else {
            log.warn(
                "Certificate of Entitlement print has missing documents. Expected documents with type {} , for Case ID: {}",
                List.of(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER, CERTIFICATE_OF_ENTITLEMENT),
                caseId);
        }
    }

    private List<Letter> certificateOfEntitlementLetters(CaseData caseData) {
        final List<Letter> coverLetters = lettersWithDocumentType(
            caseData.getDocuments().getDocumentsGenerated(),
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER);

        final Letter coverLetter = firstElement(coverLetters);
        final Letter certificateOfEntitlement =
            new Letter(caseData.getConditionalOrder().getCertificateOfEntitlementDocument(), 1);

        final List<Letter> currentCertificateOfEntitlementLetters = new ArrayList<>();

        if (null != coverLetter) {
            currentCertificateOfEntitlementLetters.add(coverLetter);
        }

        currentCertificateOfEntitlementLetters.add(certificateOfEntitlement);

        return currentCertificateOfEntitlementLetters;
    }

    private void generateCoversheet(final CaseData caseData,
                                    final Long caseId,
                                    final Applicant applicant) {

        log.info("Generating certificate of entitlement coversheet for case id {} ", caseId);

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER,
            templateVars(caseData, caseId, applicant),
            caseId,
            CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME, now(clock))
        );
    }

    private Map<String, Object> templateVars(final CaseData caseData,
                                             final Long caseId,
                                             final Applicant applicant) {

        final Map<String, Object> templateContent = new HashMap<>();

        templateContent.put(NAME, applicant.isRepresented()
            ? applicant.getSolicitor().getName()
            : join(" ", applicant.getFirstName(), applicant.getLastName())
        );
        templateContent.put(ADDRESS, applicant.getCorrespondenceAddress());
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

        final var ctscContactDetails = CtscContactDetails
            .builder()
            .emailAddress(email)
            .phoneNumber(phoneNumber)
            .build();

        templateContent.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        return templateContent;
    }
}
