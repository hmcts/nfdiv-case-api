package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Hearing;
import uk.gov.hmcts.divorce.divorcecase.model.HearingAttendance;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange.WhichApplicant;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.ApplyForConditionalOrderDocumentPack;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.FormatUtil;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.divorce.notification.EmailTemplateName.HEARING_REMINDER_CITIZEN;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.HEARING_REMINDER_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
@RequiredArgsConstructor
public class HearingReminderNotification implements ApplicantNotification {

    private final CommonContent commonContent;
    private final NotificationService notificationService;
    private final LetterPrinter letterPrinter;
    private final ApplyForConditionalOrderDocumentPack applyForConditionalOrderDocumentPack;

    private static final String HEARING_DATE = "hearingDate";
    private static final String HEARING_TIME = "hearingTime";
    private static final String HEARING_VENUE = "hearingVenue";
    private static final String HEARING_ATTENDANCE_MODE = "hearingAttendanceMode";

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

        notificationService.sendEmail(
            applicant.getEmail(),
            HEARING_REMINDER_CITIZEN,
            commonHearingReminderVariables(data, caseId, applicant, partner),
            applicant.getLanguagePreference(),
            caseId
        );
    }

    private void sendCitizenLetter(final CaseData data, final long caseId, final WhichApplicant whichApplicant) {
        final boolean isApplicant1 = WhichApplicant.APPLICANT_1.equals(whichApplicant);
        final Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();

        var documentPackInfo = applyForConditionalOrderDocumentPack.getDocumentPack(data, applicant);
        letterPrinter.sendLetters(data, caseId, applicant, documentPackInfo, applyForConditionalOrderDocumentPack.getLetterId());
    }

    private void sendSolicitorNotification(final CaseData data, final long caseId, final WhichApplicant whichApplicant) {
        final boolean isApplicant1 = WhichApplicant.APPLICANT_1.equals(whichApplicant);
        final Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        final Applicant partner =  isApplicant1 ? data.getApplicant1() : data.getApplicant2();

        final Map<String, String> templateVars = commonHearingReminderVariables(data, caseId, applicant, partner);

        templateVars.putAll(commonContent.solicitorTemplateVars(data, caseId, applicant));

        notificationService.sendEmail(
            applicant.getSolicitor().getEmail(),
            HEARING_REMINDER_SOLICITOR,
            templateVars,
            applicant.getLanguagePreference(),
            caseId
        );
    }

    private Map<String, String> commonHearingReminderVariables(
        final CaseData data, final Long caseId, final Applicant applicant, final Applicant partner
    ) {
        final Hearing hearing = data.getHearing();
        final Map<String,String> templateVars = commonContent.mainTemplateVars(data, caseId, applicant, partner);
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
