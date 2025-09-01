package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.generalApplicationLabels;
import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.populateGeneralApplicationList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralReferralPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerGeneralReferral implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_GENERAL_REFERRAL = "caseworker-general-referral";

    private static final String GENERAL_REFERRAL = "General referral";

    private final Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_GENERAL_REFERRAL)
            .forStates(POST_SUBMISSION_STATES)
            .name(GENERAL_REFERRAL)
            .description(GENERAL_REFERRAL)
            .showSummary(false)
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, SOLICITOR, CITIZEN, JUDGE))
            .page("generalReferral")
            .pageLabel(GENERAL_REFERRAL)
            .complex(CaseData::getGeneralReferral)
                .mandatory(GeneralReferral::getGeneralReferralReason)
                .mandatory(
                    GeneralReferral::getSelectedGeneralApplication,
                    "generalReferralReason=\"generalApplicationReferral\""
                )
                .mandatory(GeneralReferral::getGeneralReferralUrgentCase)
                .mandatory(GeneralReferral::getGeneralReferralUrgentCaseReason, "generalReferralUrgentCase=\"Yes\"")
                .mandatory(GeneralReferral::getGeneralReferralFraudCase)
                .mandatory(GeneralReferral::getGeneralReferralFraudCaseReason, "generalReferralFraudCase=\"Yes\"")
                .mandatory(GeneralReferral::getGeneralApplicationFrom, "generalReferralReason=\"generalApplicationReferral\"")
                .optional(GeneralReferral::getGeneralApplicationReferralDate)
                .mandatory(GeneralReferral::getGeneralReferralType)
                .mandatory(GeneralReferral::getAlternativeServiceMedium, "generalReferralType=\"alternativeServiceApplication\"")
                .mandatory(GeneralReferral::getGeneralReferralJudgeOrLegalAdvisorDetails)
                .mandatory(GeneralReferral::getGeneralReferralFeeRequired)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(
        final CaseDetails<CaseData, State> details
    ) {
        log.info("Caseworker general referral about to start callback invoked. CaseID: {}", details.getId());

        final CaseData caseData = details.getData();

        populateGeneralApplicationList(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker general referral about to submit callback invoked. CaseID: {}", details.getId());

        final CaseData caseData = details.getData();

        GeneralReferralReason referralReason = caseData.getGeneralReferral().getGeneralReferralReason();

        if (GeneralReferralReason.GENERAL_APPLICATION_REFERRAL.equals(referralReason)) {
            processSelectedGeneralApplication(caseData, details.getId());
        }
        caseData.getGeneralReferral().setSelectedGeneralApplication(null);

        State endState = caseData.getGeneralReferral().getGeneralReferralFeeRequired().toBoolean()
            ? AwaitingGeneralReferralPayment
            : AwaitingGeneralConsideration;

        caseData.getGeneralReferral().setGeneralApplicationAddedDate(LocalDate.now(clock));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(endState)
            .build();
    }

    private void processSelectedGeneralApplication(CaseData caseData, long caseId) {
        var referredApplication = caseData.getGeneralReferral().getSelectedGeneralApplication();
        if (referredApplication == null) {
            return;
        }

        for (Map.Entry<Integer, String> entry : generalApplicationLabels(caseData).entrySet()) {
            String applicationLabel = entry.getValue();
            String referredApplicationLabel = referredApplication.getValueLabel();

            if (referredApplicationLabel.equals(applicationLabel)) {
                int applicationIdx = entry.getKey();

                log.info("General application selected for referral ({}), case ID: {}", applicationLabel, caseId);

                GeneralApplication application = caseData.getGeneralApplications().get(applicationIdx).getValue();
                FeeDetails fee = application.getGeneralApplicationFee();
                boolean applicationAwaitingPayment = YesOrNo.YES.equals(application.getGeneralApplicationSubmittedOnline())
                    && fee != null
                    && fee.getServiceRequestReference() != null
                    && fee.getPaymentReference() == null;

                if (applicationAwaitingPayment) {
                    fee.setServiceRequestReference(null);
                    Applicant applicant = GeneralParties.APPLICANT.equals(application.getGeneralApplicationParty())
                        ? caseData.getApplicant1() : caseData.getApplicant2();
                    applicant.setActiveGeneralApplication(null);
                }
            }
        }
    }
}
