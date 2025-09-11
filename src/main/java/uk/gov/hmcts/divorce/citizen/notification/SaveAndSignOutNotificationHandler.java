package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.SMART_SURVEY;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.INTERIM_APPLICATION_SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;

@Component
@RequiredArgsConstructor
public class SaveAndSignOutNotificationHandler {

    public static final String INTERIM_APPLICATION_TYPE = "interimApplicationType";

    private final CcdAccessService ccdAccessService;

    private final IdamService idamService;

    private final NotificationService notificationService;

    private final CommonContent commonContent;

    public void notifyApplicant(State state, CaseData caseData, Long caseId, String userToken) {
        boolean isApplicant1 = ccdAccessService.isApplicant1(userToken, caseId);

        final User user = idamService.retrieveUser(userToken);
        Applicant applicant1 =  caseData.getApplicant1();
        Applicant applicant2 =  caseData.getApplicant2();
        final var applicant = isApplicant1 ? applicant1 : applicant2;
        final var partner = isApplicant1 ? applicant2 : applicant1;
        final var isInterimApplication = State.AosOverdue.equals(state)
            && !isEmpty(applicant1.getInterimApplicationOptions().getInterimApplicationType());

        final EmailTemplateName emailTemplate;
        if (State.InformationRequested.equals(state)) {
            emailTemplate = REQUEST_FOR_INFORMATION_SAVE_SIGN_OUT;
        } else {
            emailTemplate = isApplicant1 && isInterimApplication ? INTERIM_APPLICATION_SAVE_SIGN_OUT : SAVE_SIGN_OUT;
        }

        final var templateContent = commonContent.mainTemplateVars(caseData, caseId, applicant, partner);
        templateContent.put(SMART_SURVEY, commonContent.getSmartSurvey());

        if (isInterimApplication) {
            templateContent.put(
                INTERIM_APPLICATION_TYPE, applicant1.getInterimApplicationOptions().getInterimApplicationType()
                    .getLocalizedLabel(WELSH.equals(applicant1.getLanguagePreference())).toLowerCase());
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
