package uk.gov.hmcts.divorce.citizen.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;

@Component
public class SaveAndSignOutNotificationHandler {

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void notifyApplicant(State state, CaseData caseData, Long caseId, String userToken) {
        User user = idamService.retrieveUser(userToken);
        boolean isApplicant1 = ccdAccessService.isApplicant1(userToken, caseId);

        final var applicant = isApplicant1 ? caseData.getApplicant1() : caseData.getApplicant2();
        final var partner = isApplicant1 ? caseData.getApplicant2() : caseData.getApplicant1();

        final var emailTemplate = State.InformationRequested.equals(state) ? REQUEST_FOR_INFORMATION_SAVE_SIGN_OUT : SAVE_SIGN_OUT;
        final var templateContent = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
        templateContent.put(SMART_SURVEY, commonContent.getSmartSurvey());

        notificationService.sendEmail(
            user.getUserDetails().getSub(),
            emailTemplate,
            templateContent,
            applicant.getLanguagePreference(),
            caseId
        );
    }
}
