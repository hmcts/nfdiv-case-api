package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.Applicant1IntendToSwitchToSoleFoNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.notification.Applicant2SolicitorAppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.common.notification.ApplicantSwitchToSoleAfterIntentionFONotification;
import uk.gov.hmcts.divorce.common.notification.PartnerNotAppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.notification.SolicitorIntendsToSwitchToSoleFoNotification;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerGenerateNotifications implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private PartnerNotAppliedForFinalOrderNotification partnerNotAppliedForFinalOrderNotification;

    @Autowired
    private Applicant1IntendToSwitchToSoleFoNotification applicant1IntendToSwitchToSoleFoNotification;

    @Autowired
    private SolicitorIntendsToSwitchToSoleFoNotification solicitorIntendsToSwitchToSoleFoNotification;

    @Autowired
    private ApplicantSwitchToSoleAfterIntentionFONotification applicantSwitchToSoleAfterIntentionFONotification;

    @Autowired
    private Applicant2SolicitorAppliedForFinalOrderNotification applicant2SolicitorAppliedForFinalOrderNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event("caseworker-create-notifications")
            .forStates(POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED)
            .name("Create Notifications (TEST)")
            .description("Create Notifications (TEST)")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, JUDGE, SOLICITOR, CITIZEN, JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        details.getData().getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        details.getData().getApplicant2().setLanguagePreferenceWelsh(YesOrNo.NO);
        sendNotifications(details);

        details.getData().getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        details.getData().getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        sendNotifications(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    private void sendNotifications(final CaseDetails<CaseData, State> details) {
//        partnerNotAppliedForFinalOrderNotification.sendToApplicant1(details.getData(), details.getId());
//        applicant1IntendToSwitchToSoleFoNotification.sendToApplicant1(details.getData(), details.getId());
//        applicant1IntendToSwitchToSoleFoNotification.sendToApplicant2(details.getData(), details.getId());
//        solicitorIntendsToSwitchToSoleFoNotification.sendToApplicant2(details.getData(), details.getId());
//        applicantSwitchToSoleAfterIntentionFONotification.sendToApplicant1(details.getData(), details.getId());
        applicant1IntendToSwitchToSoleFoNotification.sendToApplicant2Solicitor(details.getData(), details.getId());
        applicantSwitchToSoleAfterIntentionFONotification.sendToApplicant1Solicitor(details.getData(), details.getId());
        applicant2SolicitorAppliedForFinalOrderNotification.sendToApplicant2Solicitor(details.getData(), details.getId());
    }
}
