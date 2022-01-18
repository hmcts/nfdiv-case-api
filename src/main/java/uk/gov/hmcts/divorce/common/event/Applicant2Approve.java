package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2ApprovedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DivorceApplicationJointTemplateContent;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateApplicant2BasicCase;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_JOINT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JOINT_DIVORCE_DRAFT_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;

@Slf4j
@Component
public class Applicant2Approve implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_2_APPROVE = "applicant2-approve";

    @Autowired
    private Applicant2ApprovedNotification applicant2ApprovedNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private DivorceApplicationJointTemplateContent divorceApplicationJointTemplateContent;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(APPLICANT_2_APPROVE)
            .forStateTransition(AwaitingApplicant2Response, Applicant2Approved)
            .name("Applicant 2 approve")
            .description("Applicant 2 has approved")
            .grant(CREATE_READ_UPDATE, APPLICANT_2, APPLICANT_2_SOLICITOR, SYSTEMUPDATE)
            .grant(READ,
                APPLICANT_1_SOLICITOR,
                CASE_WORKER,
                SUPER_USER)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Applicant 2 approve about to submit callback invoked");

        CaseData data = details.getData();

        log.info("Validating case data");
        final List<String> validationErrors = validateApplicant2BasicCase(data);

        if (!validationErrors.isEmpty()) {
            log.info("Validation errors: {} ", validationErrors);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(validationErrors)
                .state(details.getState())
                .build();
        }

        data.setDueDate(LocalDate.now().plus(2, ChronoUnit.WEEKS));

        notificationDispatcher.send(applicant2ApprovedNotification, data, details.getId());

        generateJointApplication(details, data);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(Applicant2Approved)
            .build();
    }

    private void generateJointApplication(CaseDetails<CaseData, State> details, CaseData data) {
        final long caseId = details.getId();
        final var templateVars = divorceApplicationJointTemplateContent.apply(data, caseId);

        if (nonNull(data.getApplicant1().getSolicitor()) && !isBlank(data.getApplicant1().getSolicitor().getAddress())) {
            templateVars.put(APPLICANT_1_POSTAL_ADDRESS, data.getApplicant1().getSolicitor().getAddress());
        }

        if (nonNull(data.getApplicant2().getSolicitor()) && !isBlank(data.getApplicant2().getSolicitor().getAddress())) {
            templateVars.put(APPLICANT_2_POSTAL_ADDRESS, data.getApplicant2().getSolicitor().getAddress());
        }

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            data,
            APPLICATION,
            templateVars,
            caseId,
            DIVORCE_APPLICATION_JOINT,
            data.getApplicant1().getLanguagePreference(),
            formatDocumentName(caseId, JOINT_DIVORCE_DRAFT_APPLICATION_DOCUMENT_NAME, now(clock))
        );
    }
}
