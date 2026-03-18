package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.citizen.service.InterimApplicationOptionsService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.findActiveGeneralApplicationIndex;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitizenWithdrawGeneralApplication implements CCDConfig<CaseData, State, UserRole> {
    public static final String CITIZEN_WITHDRAW_GENERAL_APPLICATION = "general-application-withdrawn";

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest httpServletRequest;

    private final InterimApplicationOptionsService interimApplicationOptionsService;

    private final DocumentRemovalService documentRemovalService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CITIZEN_WITHDRAW_GENERAL_APPLICATION)
            .forStates(POST_SUBMISSION_STATES)
            .name("General Application Withdrawn")
            .description("General Application Withdrawn")
            .showEventNotes()
            .showCondition(NEVER_SHOW)
            .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_2)
            .grantHistoryOnly(
                SOLICITOR,
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE)
            .aboutToSubmitCallback(this::aboutToSubmit));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", CITIZEN_WITHDRAW_GENERAL_APPLICATION, details.getId());

        boolean isApplicant2 = ccdAccessService.isApplicant2(httpServletRequest.getHeader(AUTHORIZATION), details.getId());

        Applicant applicant = isApplicant2 ? details.getData().getApplicant2() : details.getData().getApplicant1();
        CaseData data = details.getData();

        OptionalInt genAppIndex = findActiveGeneralApplicationIndex(data, applicant);

        Optional<GeneralApplication> removedApplication = Optional.empty();
        if (genAppIndex.isPresent()) {
            removedApplication = handleRemovalOfGeneralApplication(data, genAppIndex.getAsInt());
            applicant.setActiveGeneralApplication(null);
        } else {
            interimApplicationOptionsService.resetInterimApplicationOptions(applicant);
            applicant.getInterimApplicationOptions().setInterimApplicationType(null);
        }

        final State searchGovApplicationStartState = data.getApplication().getIssueDate() != null
            ? AwaitingAos : AwaitingDocuments;

        removedApplication.ifPresent(application -> details.setState(
            !GeneralApplicationType.DISCLOSURE_VIA_DWP.equals(application.getGeneralApplicationType())
                ? details.getState()
                : searchGovApplicationStartState
        ));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .state(details.getState())
                .build();
    }

    private Optional<GeneralApplication> handleRemovalOfGeneralApplication(CaseData data, int genAppIndex) {
        GeneralApplication generalApplication = data.getGeneralApplications().get(genAppIndex).getValue();
        if (generalApplication == null) {
            return Optional.empty();
        }
        if (generalApplication.getGeneralApplicationDocuments() != null) {
            documentRemovalService.deleteDocument(generalApplication.getGeneralApplicationDocuments());
        }
        if (generalApplication.getGeneralApplicationDocument() != null
                && generalApplication.getGeneralApplicationDocument().getDocumentLink() != null) {
            documentRemovalService.deleteDocument(generalApplication.getGeneralApplicationDocument().getDocumentLink());
        }

        List<ListValue<GeneralApplication>> mutableList = new ArrayList<>(data.getGeneralApplications());
        mutableList.remove(genAppIndex);
        data.setGeneralApplications(mutableList);

        return Optional.of(generalApplication);
    }
}
