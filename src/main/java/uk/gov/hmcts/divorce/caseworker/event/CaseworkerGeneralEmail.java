package uk.gov.hmcts.divorce.caseworker.event;

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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils.isNotEmpty;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

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
            .forStates(POST_SUBMISSION_STATES)
            .name("Create general email")
            .description("Create general email")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, SOLICITOR, CITIZEN))
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

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker create general email about to submit callback invoked");

        var caseData = details.getData();
        var generalEmail = caseData.getGeneralEmail();
        final String userAuth = httpServletRequest.getHeader(AUTHORIZATION);
        final UserDetails userDetails = idamService.retrieveUser(userAuth).getUserDetails();

        var generalEmailDetails = GeneralEmailDetails
            .builder()
            .generalEmailDateTime(LocalDateTime.now(clock))
            .generalEmailParties(generalEmail.getGeneralEmailParties())
            .generalEmailCreatedBy(userDetails.getFullName())
            .generalEmailBody(generalEmail.getGeneralEmailDetails())
            .build();

        ListValue<GeneralEmailDetails> generalEmailDetailsListValue =
            ListValue
                .<GeneralEmailDetails>builder()
                .id(UUID.randomUUID().toString())
                .value(generalEmailDetails)
                .build();

        if (isEmpty(caseData.getGeneralEmails())) {
            List<ListValue<GeneralEmailDetails>> generalEmailListValues = new ArrayList<>();
            generalEmailListValues.add(generalEmailDetailsListValue);
            caseData.setGeneralEmails(generalEmailListValues);
        } else {
            caseData.getGeneralEmails().add(0, generalEmailDetailsListValue);
        }

        generalEmailNotification.send(caseData, details.getId());

        caseData.setGeneralEmail(null); // clear existing general email

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private boolean validEmailExists(CaseData caseData) {

        GeneralParties choice = caseData.getGeneralEmail().getGeneralEmailParties();
        boolean validEmailExists = false;

        if (APPLICANT.equals(choice)) {
            Applicant applicant = caseData.getApplicant1();
            if (applicant.isRepresented()) {
                if (isNotEmpty(applicant.getSolicitor().getEmail())) {
                    validEmailExists = true;
                }
            } else if (isNotEmpty(applicant.getEmail())) {
                validEmailExists = true;
            }
        } else if (RESPONDENT.equals(choice)) {
            Applicant respondent = caseData.getApplicant2();
            if (respondent.isRepresented()) {
                if (isNotEmpty(respondent.getSolicitor().getEmail())) {
                    validEmailExists = true;
                }
            } else if (isNotEmpty(respondent.getEmail())) {
                validEmailExists = true;
            }
        } else {
            if (isNotEmpty(caseData.getGeneralEmail().getGeneralEmailOtherRecipientEmail())) {
                validEmailExists = true;
            }
        }

        return validEmailExists;
    }
}
