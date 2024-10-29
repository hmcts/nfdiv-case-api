package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.*;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REINVITE_CITIZEN_TO_CASE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
public class ReInviteApplicant1Notification implements ApplicantNotification {

    public static final String APPLICANT_2_SIGN_IN_DIVORCE_URL = "applicant2SignInDivorceUrl";
    public static final String APPLICANT_2_SIGN_IN_DISSOLUTION_URL = "applicant2SignInDissolutionUrl";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig config;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {

        log.info("Notifying applicant 1 to invite to case: {}", id);

        final Applicant applicant1 = caseData.getApplicant1();

        notificationService.sendEmail(
            applicant1.getEmail(),
            REINVITE_CITIZEN_TO_CASE,
            templateVars(caseData, id, applicant1, caseData.getApplicant2()),
            applicant1.getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long id) {

    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(ACCESS_CODE, caseData.getCaseInviteApp1().accessCode());
        templateVars.put(CREATE_ACCOUNT_LINK,
            config.getTemplateVars().get(caseData.isDivorce() ? APPLICANT_2_SIGN_IN_DIVORCE_URL : APPLICANT_2_SIGN_IN_DISSOLUTION_URL));

        return templateVars;
    }
}
