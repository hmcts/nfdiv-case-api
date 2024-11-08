package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.ACCESS_CODE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CREATE_ACCOUNT_LINK;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REINVITE_CITIZEN_TO_CASE;

@Component
@Slf4j
public class InviteApplicantToCaseNotification {

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig config;

    @Autowired
    private NotificationService notificationService;

    public static final String APPLICANT_2_SIGN_IN_DIVORCE_URL = "applicant2SignInDivorceUrl";
    public static final String APPLICANT_2_SIGN_IN_DISSOLUTION_URL = "applicant2SignInDissolutionUrl";

    public void send(final CaseData caseData, final Long caseId, final boolean isApplicant1) {

        Applicant applicant = isApplicant1 ? caseData.getApplicant1() : caseData.getApplicant2();
        Applicant partner = isApplicant1 ? caseData.getApplicant2() : caseData.getApplicant1();

        Map<String, String> templateVars = templateVars(caseData,caseId,applicant,partner);
        templateVars.put(ACCESS_CODE, isApplicant1 ? caseData.getCaseInviteApp1().accessCode()
            : caseData.getCaseInvite().accessCode());

        notificationService.sendEmail(
            applicant.getEmail(),
            REINVITE_CITIZEN_TO_CASE,
            templateVars,
            applicant.getLanguagePreference(),
            caseId
        );
    }

    private Map<String, String> templateVars(CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(CREATE_ACCOUNT_LINK,
            config.getTemplateVars().get(caseData.isDivorce() ? APPLICANT_2_SIGN_IN_DIVORCE_URL : APPLICANT_2_SIGN_IN_DISSOLUTION_URL));

        return templateVars;
    }
}
