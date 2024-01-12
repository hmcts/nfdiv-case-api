package uk.gov.hmcts.divorce.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.notification.GeneralEmailNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmail;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmailDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentUtil.isConfidential;
import static uk.gov.hmcts.divorce.document.model.DocumentType.EMAIL;

@Component
@Slf4j
public class CaseworkerGeneralEmail implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CREATE_GENERAL_EMAIL = "caseworker-create-general-email";

    private static final String NO_VALID_EMAIL_ERROR
        = "You cannot send an email because no email address has been provided for this party.";

    @Autowired
    private GeneralEmailNotification generalEmailNotification;

    @Autowired
    private IdamService idamService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CREATE_GENERAL_EMAIL)
            .forStates(POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED)
            .name("Create general email")
            .description("Create general email")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, JUDGE, SOLICITOR, CITIZEN, JUDGE))
            .page("createGeneralEmail", this::midEvent)
            .pageLabel("Create general email")
            .complex(CaseData::getGeneralEmail)
            .mandatory(GeneralEmail::getGeneralEmailParties)
            .mandatory(GeneralEmail::getGeneralEmailOtherRecipientEmail, "generalEmailParties=\"other\"")
            .mandatory(GeneralEmail::getGeneralEmailOtherRecipientName, "generalEmailParties=\"other\"")
            .mandatory(GeneralEmail::getGeneralEmailDetails)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {


        final CaseData caseData = details.getData();

        if (!validEmailExists(caseData)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(NO_VALID_EMAIL_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker create general email about to submit callback invoked for Case Id: {}", details.getId());

        var caseData = details.getData();
        var generalEmail = caseData.getGeneralEmail();
        final String userAuth = httpServletRequest.getHeader(AUTHORIZATION);
        final var userDetails = idamService.retrieveUser(userAuth).getUserDetails();

        var generalEmailDetails = GeneralEmailDetails
            .builder()
            .generalEmailDateTime(LocalDateTime.now(clock))
            .generalEmailParties(generalEmail.getGeneralEmailParties())
            .generalEmailCreatedBy(userDetails.getName())
            .generalEmailBody(generalEmail.getGeneralEmailDetails())
            .build();

        ListValue<GeneralEmailDetails> generalEmailDetailsListValue =
            ListValue
                .<GeneralEmailDetails>builder()
                .id(UUID.randomUUID().toString())
                .value(generalEmailDetails)
                .build();


        if (isConfidential(caseData, EMAIL)) {
            if (isEmpty(caseData.getConfidentialGeneralEmails())) {
                caseData.setConfidentialGeneralEmails(List.of(generalEmailDetailsListValue));
            } else {
                caseData.getConfidentialGeneralEmails().add(0, generalEmailDetailsListValue);
            }
        } else {
            if (isEmpty(caseData.getGeneralEmails())) {
                caseData.setGeneralEmails(List.of(generalEmailDetailsListValue));
            } else {
                caseData.getGeneralEmails().add(0, generalEmailDetailsListValue);
            }
        }

        generalEmailNotification.send(caseData, details.getId());

        // clear existing general email to avoid stale data being displayed in UI on next use of event.
        caseData.setGeneralEmail(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public boolean validEmailExists(CaseData caseData) {
        GeneralParties recipient = caseData.getGeneralEmail().getGeneralEmailParties();

        return switch (recipient) {
            case APPLICANT -> isEmailValid(caseData.getApplicant1());
            case RESPONDENT -> isEmailValid(caseData.getApplicant2());
            case OTHER -> isNotEmpty(caseData.getGeneralEmail().getGeneralEmailOtherRecipientEmail());
        };
    }

    private boolean isEmailValid(Applicant applicant) {
        if (applicant.isRepresented()) {
            return isNotEmpty(applicant.getSolicitor().getEmail());
        } else {
            return isNotEmpty(applicant.getEmail());
        }
    }
}
