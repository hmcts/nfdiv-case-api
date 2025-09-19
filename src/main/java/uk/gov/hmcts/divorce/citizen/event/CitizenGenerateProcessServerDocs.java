package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplicant2NoticeOfProceedings;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplication;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.caseworker.service.task.SetNoticeOfProceedingDetailsForRespondent;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Archived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFEvidence;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFPartPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.NewPaperCase;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Rejected;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateServiceDate;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.flattenLists;

@Slf4j
@Component
@RequiredArgsConstructor
public class CitizenGenerateProcessServerDocs implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_GENERATE_PROCESS_SERVER_DOCS = "citizen-generate-process-server-docs";
    public static final String CONFIDENTIAL_RESPONDENT_ERROR = "Unable to generate process server docs, the respondent is confidential";

    private final GenerateApplication generateApplication;
    private final GenerateApplicant2NoticeOfProceedings generateApplicant2NoticeOfProceedings;
    private final GenerateD10Form generateD10Form;
    private final SetNoticeOfProceedingDetailsForRespondent setNoticeOfProceedingDetailsForRespondent;

    @Value("${interim_application.repeat_service_offset_days}")
    private int docsRegeneratedOffsetDays;

    private static final EnumSet<State> CITIZEN_UPDATE_STATES = EnumSet.complementOf(EnumSet.of(
        AwaitingApplicant2Response,
        Applicant2Approved,
        AwaitingPayment,
        AwaitingHWFDecision,
        AwaitingHWFEvidence,
        AwaitingHWFPartPayment,
        AwaitingDocuments,
        AwaitingService,
        NewPaperCase,
        Submitted,
        Withdrawn,
        Rejected,
        Archived
    ));

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(CITIZEN_GENERATE_PROCESS_SERVER_DOCS)
            .forStates(CITIZEN_UPDATE_STATES)
            .showCondition(NEVER_SHOW)
            .name("Generate Process Server Docs")
            .description("Citizen event to generate docs for process server")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CREATOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} aboutToStart callback invoked for Case Id: {}", CITIZEN_GENERATE_PROCESS_SERVER_DOCS, details.getId());

        CaseData caseData = details.getData();

        List<String> validationErrors = flattenLists(
            validateRespondentConfidentiality(caseData),
            validateServiceDate(caseData, docsRegeneratedOffsetDays)
        );

        if (isNotEmpty(validationErrors)) {
            log.info("Rejected citizen request to generate process server documents, Case Id: {}", details.getId());

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(validationErrors)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} aboutToSubmit callback invoked for Case Id: {}", CITIZEN_GENERATE_PROCESS_SERVER_DOCS, details.getId());

        CaseData caseData = details.getData();

        caseData.getApplication().setServiceDocumentsRegeneratedDate(LocalDate.now());

        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        CaseData data = caseTasks(
            setNoticeOfProceedingDetailsForRespondent,
            generateApplicant2NoticeOfProceedings,
            generateApplication,
            generateD10Form
        ).run(details).getData();

        caseData.getApplicant1().setInterimApplicationOptions(
            buildProcessServerOptions()
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(AwaitingService)
            .build();
    }

    private List<String> validateRespondentConfidentiality(CaseData caseData) {
        List<String> validationErrors = new ArrayList<>();

        boolean respondentIsConfidential = caseData.getApplicant2().isConfidentialContactDetails();
        if (respondentIsConfidential) {
            validationErrors.add(CONFIDENTIAL_RESPONDENT_ERROR);
        }

        return validationErrors;
    }

    private InterimApplicationOptions buildProcessServerOptions() {
        return InterimApplicationOptions.builder()
            .interimApplicationType(InterimApplicationType.PROCESS_SERVER_SERVICE)
            .build();
    }
}
