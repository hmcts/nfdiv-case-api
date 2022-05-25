package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.RespondentSolicitorReminderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class SystemRemindRespondentSolicitor implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND = "system-remind-respondent-solicitor-to-respond";

    @Autowired
    private RespondentSolicitorReminderNotification respondentSolicitorReminderNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Value("${respondent_solicitor.response_offset_days}")
    private int responseReminderOffsetDays;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND)
            .forState(AwaitingAos)
            .name("Remind Respondent Solicitor")
            .description("Remind Respondent Solicitor to respond to the application (Notice of Proceedings)")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData data = details.getData();

        //to avoid incorrect event submission in case of system user manually submits this event
        if (isCaseValidForThisSubmission(data)) {
            notificationDispatcher.send(respondentSolicitorReminderNotification, data, details.getId());
            data.getApplication().setRespondentSolicitorReminderSent(YesOrNo.YES);
        } else {

            log.error("Case data validation failed for this event for case {}", details.getId());

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("Case data is not valid to submit this event"))
                .data(data)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    private boolean isCaseValidForThisSubmission(final CaseData data) {
        Applicant respondent = data.getApplicant2();
        Application application = data.getApplication();

        return SOLE_APPLICATION.equals(data.getApplicationType())
            && COURT_SERVICE.equals(application.getServiceMethod())
            && respondent.isRepresented()
            && respondent.getSolicitor().hasOrgId()
            && StringUtils.isNotBlank(respondent.getSolicitor().getEmail())
            && !LocalDate.now(clock).minusDays(responseReminderOffsetDays).isBefore(application.getIssueDate());
    }
}
