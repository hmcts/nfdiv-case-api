package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Hearing;
import uk.gov.hmcts.divorce.divorcecase.model.HearingAttendance;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange.WhichApplicant;
import uk.gov.hmcts.divorce.divorcecase.util.AddressUtil;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.FormatUtil;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.divorce.document.DocumentConstants.HEARING_REMINDER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.HEARING_REMINDER_CITIZEN;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.HEARING_REMINDER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
@RequiredArgsConstructor
public class HearingReminderNotification implements ApplicantNotification {

    private final CommonContent commonContent;
    private final NotificationService notificationService;
    private final CaseDataDocumentService caseDataDocumentService;
    private final BulkPrintService bulkPrintService;

    private static final String HEARING_DATE = "hearingDate";
    private static final String HEARING_TIME = "hearingTime";
    private static final String HEARING_VENUE = "hearingVenue";
    private static final String HEARING_ATTENDANCE_MODE = "hearingAttendanceMode";

    private static final String LETTER_TYPE = "hearing-reminder-letter";
    private final DocmosisCommonContent docmosisCommonContent;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending hearing reminder notification to applicant 1: {}", id);

        sendCitizenNotification(caseData, id, WhichApplicant.APPLICANT_1);
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Sending hearing reminder notification to applicant 2/respondent: {}", id);

        sendCitizenNotification(caseData, id, WhichApplicant.APPLICANT_2);
    }

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long id) {
        log.info("Sending hearing reminder letter to applicant 1: {}", id);

        sendCitizenLetter(caseData, id, WhichApplicant.APPLICANT_1);
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, final Long id) {
        log.info("Sending hearing reminder letter to applicant 2/respondent: {}", id);

        sendCitizenLetter(caseData, id, WhichApplicant.APPLICANT_2);
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        log.info("Sending hearing reminder notification to applicant 1 solicitor: {}", id);

        sendSolicitorNotification(caseData, id, WhichApplicant.APPLICANT_1);
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        log.info("Sending hearing reminder notification to applicant 2/respondent solicitor: {}", id);

        sendSolicitorNotification(caseData, id, WhichApplicant.APPLICANT_2);
    }

    private void sendCitizenNotification(final CaseData data, final long caseId, final WhichApplicant whichApplicant) {
        final boolean isApplicant1 = WhichApplicant.APPLICANT_1.equals(whichApplicant);
        final Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        final Applicant partner = isApplicant1 ? data.getApplicant2() : data.getApplicant1();

        final Map<String, String> templateVars = commonContent.mainTemplateVars(data, caseId, applicant, partner);
        templateVars.putAll(hearingReminderVariables(data, applicant, partner));

        notificationService.sendEmail(
            applicant.getEmail(),
            HEARING_REMINDER_CITIZEN,
            templateVars,
            applicant.getLanguagePreference(),
            caseId
        );
    }

    private void sendSolicitorNotification(final CaseData data, final long caseId, final WhichApplicant whichApplicant) {
        final boolean isApplicant1 = WhichApplicant.APPLICANT_1.equals(whichApplicant);
        final Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        final Applicant partner =  isApplicant1 ? data.getApplicant1() : data.getApplicant2();

        final Map<String, String> templateVars = commonContent.solicitorTemplateVars(data, caseId, applicant);
        templateVars.putAll(hearingReminderVariables(data, applicant, partner));

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            HEARING_REMINDER_SOLICITOR,
            templateVars,
            applicant.getLanguagePreference(),
            caseId
        );
    }

    private void sendCitizenLetter(final CaseData data, final long caseId, final WhichApplicant whichApplicant) {
        final boolean isApplicant1 = WhichApplicant.APPLICANT_1.equals(whichApplicant);
        final Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        final Applicant partner = isApplicant1 ? data.getApplicant2() : data.getApplicant1();

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference());
        templateContent.putAll(hearingReminderVariables(data, applicant, partner));
        templateContent.put(NAME, applicant.getFullName());
        templateContent.put(ADDRESS, AddressUtil.getPostalAddress(applicant.getAddress()));
        templateContent.put(IS_DIVORCE, data.isDivorce());
        templateContent.put(CASE_REFERENCE, formatId(caseId));

        final Document generatedDocument = generateDocument(caseId, applicant, templateContent);

        final Letter letter = new  Letter(generatedDocument, 1);
        String caseIdString = String.valueOf(caseId);

        final Print print = new Print(
            List.of(letter),
            caseIdString,
            caseIdString,
            LETTER_TYPE,
            applicant.getFullName(),
            applicant.getAddressOverseas()
        );

        final UUID letterId = bulkPrintService.print(print);

        log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
    }

    private Document generateDocument(final long caseId, final Applicant applicant, Map<String,Object> templateContent) {
        return caseDataDocumentService.renderDocument(
            templateContent,
            caseId,
            HEARING_REMINDER,
            applicant.getLanguagePreference(),
            LETTER_TYPE
        );
    }

    private Map<String, String> hearingReminderVariables(final CaseData data, final Applicant applicant, final Applicant partner) {
        final Map<String,String> templateVars = new HashMap<>();
        final Hearing hearing = data.getHearing();
        final DateTimeFormatter dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference());
        final String hearingAttendanceLabel = hearing.getHearingAttendance().stream()
            .map(HearingAttendance::getLabel)
            .collect(Collectors.joining(" / "));

        templateVars.put(HEARING_DATE, hearing.getDateOfHearing().format(dateTimeFormatter));
        templateVars.put(HEARING_VENUE, hearing.getVenueOfHearing());
        templateVars.put(HEARING_TIME, hearing.getDateOfHearing().format(FormatUtil.TIME_FORMATTER));
        templateVars.put(HEARING_ATTENDANCE_MODE, hearingAttendanceLabel);

        return templateVars;
    }
}
