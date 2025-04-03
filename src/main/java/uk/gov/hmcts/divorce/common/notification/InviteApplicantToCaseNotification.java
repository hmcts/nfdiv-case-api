package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class InviteApplicantToCaseNotification {

    private final CommonContent commonContent;

    private final EmailTemplatesConfig config;

    private final NotificationService notificationService;

    private static final String APPLICANT_2_SIGN_IN_DIVORCE_URL = "applicant2SignInDivorceUrl";
    private static final String APPLICANT_2_SIGN_IN_DISSOLUTION_URL = "applicant2SignInDissolutionUrl";
    public static final String RESPONDENT_SIGN_IN_DIVORCE_URL = "respondentSignInDivorceUrl";
    public static final String RESPONDENT_SIGN_IN_DISSOLUTION_URL = "respondentSignInDissolutionUrl";
    private static final String SIGN_IN_DIVORCE_URL = "signInDivorceUrl";
    private static final String SIGN_IN_DISSOLUTION_URL = "signInDissolutionUrl";

    public void send(final CaseData caseData, final Long caseId, final boolean isApplicant1) {

        Applicant applicant = isApplicant1 ? caseData.getApplicant1() : caseData.getApplicant2();
        Applicant partner = isApplicant1 ? caseData.getApplicant2() : caseData.getApplicant1();

        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
        templateVars.put(ACCESS_CODE, isApplicant1 ? caseData.getCaseInviteApp1().accessCodeApplicant1()
            : caseData.getCaseInvite().accessCode());

        if (isApplicant1) {
            templateVars.put(CREATE_ACCOUNT_LINK,
                config.getTemplateVars().get(caseData.isDivorce() ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL));
        } else {
            if (caseData.getApplicationType().isSole()) {
                templateVars.put(CREATE_ACCOUNT_LINK,
                    config.getTemplateVars().get(caseData.isDivorce() ? RESPONDENT_SIGN_IN_DIVORCE_URL
                        : RESPONDENT_SIGN_IN_DISSOLUTION_URL));
            } else {
                templateVars.put(CREATE_ACCOUNT_LINK,
                    config.getTemplateVars().get(caseData.isDivorce() ? APPLICANT_2_SIGN_IN_DIVORCE_URL
                        : APPLICANT_2_SIGN_IN_DISSOLUTION_URL));
            }
        }

        notificationService.sendEmail(
            applicant.getEmail(),
            REINVITE_CITIZEN_TO_CASE,
            templateVars,
            applicant.getLanguagePreference(),
            caseId
        );
    }
}
