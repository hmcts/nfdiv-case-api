package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_ALTERNATIVE_SERVICE;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_BAILIFF_SERVICE;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_DEEMED_SERVICE;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_DISPENSE_SERVICE;
import static uk.gov.hmcts.divorce.citizen.notification.GeneralApplicationReceivedNotification.IS_SEARCH_GOV_SERVICE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.INTERIM_APPLICATION_SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;

@Component
@RequiredArgsConstructor
public class SaveAndSignOutNotificationHandler {

    private final CcdAccessService ccdAccessService;

    private final IdamService idamService;

    private final NotificationService notificationService;

    private final CommonContent commonContent;

    public void notifyApplicant(State state, CaseData caseData, Long caseId, String userToken) {
        User user = idamService.retrieveUser(userToken);
        boolean isApplicant1 = ccdAccessService.isApplicant1(userToken, caseId);

        Applicant applicant1 =  caseData.getApplicant1();
        Applicant applicant2 =  caseData.getApplicant2();
        final var applicant = isApplicant1 ? applicant1 : applicant2;
        final var partner = isApplicant1 ? applicant2 : applicant1;
        final var isInterimApplication = State.AosOverdue.equals(state) && !isEmpty(applicant1.getInterimApplicationOptions());

        final var emailTemplate = State.InformationRequested.equals(state)
            ? REQUEST_FOR_INFORMATION_SAVE_SIGN_OUT : isInterimApplication ? INTERIM_APPLICATION_SAVE_SIGN_OUT : SAVE_SIGN_OUT;
        final var templateContent = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
        templateContent.put(SMART_SURVEY, commonContent.getSmartSurvey());

        if(isInterimApplication) {
            switch (applicant1.getInterimApplicationOptions().getInterimApplicationType()) {
                case DEEMED_SERVICE -> templateContent.put(IS_DEEMED_SERVICE, "deemed service");
                case BAILIFF_SERVICE -> templateContent.put(IS_BAILIFF_SERVICE, "bailiff service");
                case ALTERNATIVE_SERVICE -> templateContent.put(IS_ALTERNATIVE_SERVICE, "alternative service");
                case DISPENSE_WITH_SERVICE -> templateContent.put(IS_DISPENSE_SERVICE, "dispense with service");
                default -> templateContent.put(IS_SEARCH_GOV_SERVICE, "search government records"); // Maybe we could have it as a case or leave it as default
            }
        }

        notificationService.sendEmail(
            user.getUserDetails().getSub(),
            emailTemplate,
            templateContent,
            applicant.getLanguagePreference(),
            caseId
        );
    }
}
