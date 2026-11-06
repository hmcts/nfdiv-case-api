package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;
import java.util.OptionalInt;

import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.findGeneralApplicationIndexByLabel;
import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.populateUnpaidGeneralApplicationList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerGeneralApplicationPayment implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_GENERAL_APPLICATION_PAYMENT = "caseworker-general-app-payment";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_GENERAL_APPLICATION_PAYMENT)
            .forStates(POST_SUBMISSION_STATES)
            .name("General application payment")
            .description("General application payment")
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, JUDGE, SOLICITOR, CITIZEN, JUDGE))
            .page("generalApplicationPayment")
                .pageLabel("General application payment")
                .complex(CaseData::getGeneralReferral)
                .mandatoryWithLabel(GeneralReferral::getSelectedGeneralApplication, "Which general application?")
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_GENERAL_APPLICATION_PAYMENT, details.getId());

        log.info("Retrieving active general applications for Case Id: {}", details.getId());
        final CaseData caseData = details.getData();

        populateUnpaidGeneralApplicationList(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(null)
            .warnings(null)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> detailsBefore) {
        CaseData caseData = details.getData();

        String generalApplicationSelected =
                caseData.getGeneralReferral().getSelectedGeneralApplication().getValue().getLabel();

        OptionalInt genAppIdx = findGeneralApplicationIndexByLabel(caseData, generalApplicationSelected);

        if (genAppIdx.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(details.getData())
                    .state(details.getState())
                    .build();
        }

        GeneralApplication generalApplication = caseData.getGeneralApplications().get(genAppIdx.getAsInt()).getValue();
        caseData.getGeneralApplications().get(genAppIdx.getAsInt()).getValue()
                .recordAlternatePayment(ServicePaymentMethod.FEE_PAY_BY_PHONE);

        if (generalApplication.getGeneralApplicationParty() != null) {
            Applicant applicant = GeneralParties.APPLICANT.equals(generalApplication.getGeneralApplicationParty())
                    ? caseData.getApplicant1()
                    : caseData.getApplicant2();

            if (applicant.getGeneralAppServiceRequest() != null
                    && applicant.getGeneralAppServiceRequest()
                    .equals(generalApplication.getGeneralApplicationFee().getServiceRequestReference())) {
                applicant.setActiveGeneralApplication(null);
            }
        }

        caseData.getGeneralReferral().setSelectedGeneralApplication(null);

        setEndState(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .state(details.getState())
                .build();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> getAboutToSubmitResponse(CaseData caseData, List<String> validationErrors) {
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(validationErrors)
            .build();
    }

    private void setEndState(CaseDetails<CaseData, State> details) {
        final CaseData data = details.getData();
        if (data.isWelshApplication()) {
            data.getApplication().setWelshPreviousState(details.getState());
            details.setState(WelshTranslationReview);
        } else {
            details.setState(GeneralApplicationReceived);
        }
    }
}
