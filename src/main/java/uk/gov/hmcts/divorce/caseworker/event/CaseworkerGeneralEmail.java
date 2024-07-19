package uk.gov.hmcts.divorce.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
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
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static java.util.stream.Stream.ofNullable;
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

    public static final int MAX_NUMBER_GENERAL_EMAIL_ATTACHMENTS = 10;

    public static final String CASEWORKER_CREATE_GENERAL_EMAIL = "caseworker-create-general-email";

    private static final String NO_VALID_EMAIL_ERROR
        = "You cannot send an email because no email address has been provided for this party.";

    private static final String WARNING_ATTACHMENT_SIZE = "### NOTE: Individual attachments must be less than 2MB"
        + " or else the general email will fail to send";

    @Autowired
    private DocumentIdProvider documentIdProvider;

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
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, JUDGE, SOLICITOR, CITIZEN, JUDGE))
            .page("createGeneralEmail", this::midEvent)
            .pageLabel("Create general email")
            .complex(CaseData::getGeneralEmail)
            .mandatory(GeneralEmail::getGeneralEmailParties)
            .mandatory(GeneralEmail::getGeneralEmailOtherRecipientEmail, "generalEmailParties=\"other\"")
            .mandatory(GeneralEmail::getGeneralEmailOtherRecipientName, "generalEmailParties=\"other\"")
            .mandatory(GeneralEmail::getGeneralEmailDetails)
            .label("attachmentWarning", WARNING_ATTACHMENT_SIZE)
            .optional(GeneralEmail::getGeneralEmailAttachments)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_CREATE_GENERAL_EMAIL, details.getId());
        CaseData caseData = details.getData();

        //Setting generalEmail to null to ensure stale data is not present when event is launched next
        caseData.setGeneralEmail(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {


        final CaseData caseData = details.getData();

        if (!validEmailExists(caseData)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(NO_VALID_EMAIL_ERROR))
                .build();
        }

        final boolean invalidGeneralEmailAttachments = ofNullable(caseData.getGeneralEmail().getGeneralEmailAttachments())
            .flatMap(Collection::stream)
            .anyMatch(divorceDocument -> ObjectUtils.isEmpty(divorceDocument.getValue().getDocumentLink()));

        if (invalidGeneralEmailAttachments) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("Please ensure all General Email attachments have been uploaded before continuing"))
                .build();
        }

        if (caseData.getGeneralEmail().getGeneralEmailAttachments() != null
            && caseData.getGeneralEmail().getGeneralEmailAttachments().size() > MAX_NUMBER_GENERAL_EMAIL_ATTACHMENTS) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList(String.format("Number of attachments on General Email cannot exceed %s",
                    MAX_NUMBER_GENERAL_EMAIL_ATTACHMENTS)))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_CREATE_GENERAL_EMAIL, details.getId());

        var caseData = details.getData();
        var generalEmail = caseData.getGeneralEmail();
        final String userAuth = httpServletRequest.getHeader(AUTHORIZATION);
        final var userDetails = idamService.retrieveUser(userAuth).getUserDetails();

        List<ListValue<Document>> attachments = ofNullable(generalEmail.getGeneralEmailAttachments())
            .flatMap(Collection::stream)
            .map(divorceDocument -> ListValue.<Document>builder()
                .id(documentIdProvider.documentId())
                .value(divorceDocument.getValue().getDocumentLink()).build())
            .toList();

        var generalEmailDetails = GeneralEmailDetails
            .builder()
            .generalEmailDateTime(LocalDateTime.now(clock))
            .generalEmailParties(generalEmail.getGeneralEmailParties())
            .generalEmailCreatedBy(userDetails.getName())
            .generalEmailBody(generalEmail.getGeneralEmailDetails())
            .generalEmailAttachmentLinks(attachments)
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

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} submitted callback invoked for Case Id: {}",CASEWORKER_CREATE_GENERAL_EMAIL, details.getId());

        CaseData caseData = details.getData();

        //Likely the attached document isn't available in CDAM before aboutToSubmit callback has completed so
        //to avoid CDAM issues during notification, moving the send call to submitted callback

        try {
            generalEmailNotification.send(caseData, details.getId());
        } catch (NotificationClientException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return SubmittedCallbackResponse.builder().build();
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
