package uk.gov.hmcts.divorce.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
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
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Stream.ofNullable;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
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

    private static final String WARNING_ATTACHMENTS
        = "\n ### WARNING: Please check that you have uploaded/selected the correct documents and recipient. \n";

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
            .label("attachmentWarning", WARNING_ATTACHMENTS)
            .readonly(GeneralEmail::getGeUploadedDocumentNames)
            .readonly(GeneralEmail::getGeGeneratedDocumentNames)
            .readonly(GeneralEmail::getGeScannedDocumentNames)
            .readonly(GeneralEmail::getGeApplicant1DocumentNames)
            .readonly(GeneralEmail::getGeApplicant2DocumentNames)
            .readonly(GeneralEmail::getGeAttachedDocumentNames)
            .readonlyWithLabel(GeneralEmail::getGeUploadedDocumentNames, "Uploaded documents selected")
            .readonlyWithLabel(GeneralEmail::getGeGeneratedDocumentNames, "Generated documents selected")
            .readonlyWithLabel(GeneralEmail::getGeScannedDocumentNames, "Scanned documents selected")
            .readonlyWithLabel(GeneralEmail::getGeApplicant1DocumentNames, "Applicant 1 documents selected")
            .readonlyWithLabel(GeneralEmail::getGeApplicant2DocumentNames, "Applicant 2 documents selected")
            .readonlyWithLabel(GeneralEmail::getGeAttachedDocumentNames,"Attached documents")
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
        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_CREATE_GENERAL_EMAIL, details.getId());

        final CaseData caseData = details.getData();
        final GeneralEmail generalEmail = caseData.getGeneralEmail();

        var caseDataCopy = caseData.toBuilder().build();
        populateSelectedDocsToAttachedList(caseDataCopy);

        final String userAuth = httpServletRequest.getHeader(AUTHORIZATION);
        final var userDetails = idamService.retrieveUser(userAuth).getUserDetails();

        List<ListValue<Document>> attachments = ofNullable(caseDataCopy.getGeneralEmail().getGeneralEmailAttachments())
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

        List<String> errors = new ArrayList<String>();

        try {
            generalEmailNotification.send(caseDataCopy, details.getId());
        } catch (NotificationClientException e) {
            log.info("{} about to submit failed due to NotificationClientException : {}", CASEWORKER_CREATE_GENERAL_EMAIL, e.getMessage());
            errors.add(e.getMessage());
        } catch (IOException e) {
            log.info("{} about to submit failed due to IOException : {}", CASEWORKER_CREATE_GENERAL_EMAIL, e.getMessage());
            errors.add(e.getMessage());
        }

        caseData.setGeneralEmail(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
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

    void populateSelectedDocsToAttachedList(final CaseData caseData) {
        if (caseData.getGeneralEmail().getGeScannedDocumentNames() != null
            && caseData.getGeneralEmail().getGeScannedDocumentNames().getValue().size() > 0) {
            addSelectedScannedDocuments(caseData);
        }

        if (caseData.getGeneralEmail().getGeUploadedDocumentNames() != null
            && caseData.getGeneralEmail().getGeUploadedDocumentNames().getValue().size() > 0) {
            addSelectedUploadedDocuments(caseData);
        }

        if (caseData.getGeneralEmail().getGeGeneratedDocumentNames() != null
            && caseData.getGeneralEmail().getGeGeneratedDocumentNames().getValue().size() > 0) {
            addSelectedGeneratedDocuments(caseData);
        }

        if (caseData.getGeneralEmail().getGeApplicant1DocumentNames() != null
            && caseData.getGeneralEmail().getGeApplicant1DocumentNames().getValue().size() > 0) {
            addSelectedApp1Documents(caseData);
        }

        if (caseData.getGeneralEmail().getGeApplicant2DocumentNames() != null
            && caseData.getGeneralEmail().getGeApplicant2DocumentNames().getValue().size() > 0) {
            addSelectedApp2Documents(caseData);
        }
    }

    void  addSelectedScannedDocuments(final CaseData caseData) {
        GeneralEmail generalEmail = caseData.getGeneralEmail();

        List<ListValue<DivorceDocument>> listOfAttachments = new ArrayList<>();

        final List<DynamicListElement> selectedDocuments = generalEmail.getGeScannedDocumentNames().getValue();

        for (DynamicListElement element : selectedDocuments) {
            UUID uuidCode = element.getCode();
            Optional<ListValue<ScannedDocument>> uploadedDocumentOptional =
                emptyIfNull(caseData.getDocuments().getScannedDocuments())
                    .stream()
                    .filter(doc -> UUID.fromString(doc.getId()).equals(uuidCode))
                    .findFirst();

            if (uploadedDocumentOptional.isPresent()) {
                ListValue<DivorceDocument> emailDoc =
                    ListValue.<DivorceDocument>builder()
                        .id(documentIdProvider.documentId())
                        .value(DivorceDocument.builder().documentLink(uploadedDocumentOptional.get().getValue().getUrl()).build())
                        .build();
                listOfAttachments.add(emailDoc);
            }
        }
        addListToGeneralEmailAttachments(caseData, listOfAttachments);
    }

    void addSelectedUploadedDocuments(final CaseData caseData) {
        final GeneralEmail generalEmail = caseData.getGeneralEmail();

        List<ListValue<DivorceDocument>> listOfAttachments = new ArrayList<>();

        final List<DynamicListElement> selectedDocuments = generalEmail.getGeUploadedDocumentNames().getValue();

        for (DynamicListElement element : selectedDocuments) {
            UUID uuidCode = element.getCode();
            Optional<ListValue<DivorceDocument>> uploadedDocumentOptional =
                emptyIfNull(caseData.getDocuments().getDocumentsUploaded())
                    .stream()
                    .filter(doc -> UUID.fromString(doc.getId()).equals(uuidCode))
                    .findFirst();

            if (uploadedDocumentOptional.isPresent()) {
                ListValue<DivorceDocument> emailDoc =
                    ListValue.<DivorceDocument>builder()
                        .id(documentIdProvider.documentId())
                        .value(DivorceDocument.builder().documentLink(uploadedDocumentOptional.get().getValue().getDocumentLink()).build())
                        .build();
                listOfAttachments.add(emailDoc);
            }
        }
        addListToGeneralEmailAttachments(caseData, listOfAttachments);
    }

    void addSelectedGeneratedDocuments(final CaseData caseData) {
        final GeneralEmail generalEmail = caseData.getGeneralEmail();

        List<ListValue<DivorceDocument>> listOfAttachments = new ArrayList<>();

        final List<DynamicListElement> selectedDocuments = generalEmail.getGeGeneratedDocumentNames().getValue();

        for (DynamicListElement element : selectedDocuments) {
            UUID uuidCode = element.getCode();
            Optional<ListValue<DivorceDocument>> uploadedDocumentOptional =
                emptyIfNull(caseData.getDocuments().getDocumentsGenerated())
                    .stream()
                    .filter(doc -> UUID.fromString(doc.getId()).equals(uuidCode))
                    .findFirst();

            if (uploadedDocumentOptional.isPresent()) {
                ListValue<DivorceDocument> emailDoc =
                    ListValue.<DivorceDocument>builder()
                        .id(documentIdProvider.documentId())
                        .value(DivorceDocument.builder().documentLink(uploadedDocumentOptional.get().getValue().getDocumentLink()).build())
                        .build();
                listOfAttachments.add(emailDoc);
            }
        }
        addListToGeneralEmailAttachments(caseData, listOfAttachments);
    }

    void addSelectedApp1Documents(final CaseData caseData) {
        GeneralEmail generalEmail = caseData.getGeneralEmail();

        List<ListValue<DivorceDocument>> listOfAttachments = new ArrayList<>();

        final List<DynamicListElement> selectedDocuments = generalEmail.getGeApplicant1DocumentNames().getValue();

        for (DynamicListElement element : selectedDocuments) {
            UUID uuidCode = element.getCode();
            Optional<ListValue<DivorceDocument>> uploadedDocumentOptional =
                emptyIfNull(caseData.getDocuments().getApplicant1DocumentsUploaded())
                    .stream()
                    .filter(doc -> UUID.fromString(doc.getId()).equals(uuidCode))
                    .findFirst();

            if (uploadedDocumentOptional.isPresent()) {
                ListValue<DivorceDocument> emailDoc =
                    ListValue.<DivorceDocument>builder()
                        .id(documentIdProvider.documentId())
                        .value(DivorceDocument.builder().documentLink(uploadedDocumentOptional.get().getValue().getDocumentLink()).build())
                        .build();
                listOfAttachments.add(emailDoc);
            }
        }
        addListToGeneralEmailAttachments(caseData, listOfAttachments);
    }

    void addSelectedApp2Documents(final CaseData caseData) {
        GeneralEmail generalEmail = caseData.getGeneralEmail();

        List<ListValue<DivorceDocument>> listOfAttachments = new ArrayList<>();

        final List<DynamicListElement> selectedDocuments = generalEmail.getGeApplicant2DocumentNames().getValue();

        for (DynamicListElement element : selectedDocuments) {
            UUID uuidCode = element.getCode();
            Optional<ListValue<DivorceDocument>> uploadedDocumentOptional =
                emptyIfNull(caseData.getDocuments().getApplicant2DocumentsUploaded())
                    .stream()
                    .filter(doc -> UUID.fromString(doc.getId()).equals(uuidCode))
                    .findFirst();

            if (uploadedDocumentOptional.isPresent()) {
                ListValue<DivorceDocument> emailDoc =
                    ListValue.<DivorceDocument>builder()
                        .id(documentIdProvider.documentId())
                        .value(DivorceDocument.builder().documentLink(uploadedDocumentOptional.get().getValue().getDocumentLink()).build())
                        .build();
                listOfAttachments.add(emailDoc);
            }
        }
        addListToGeneralEmailAttachments(caseData, listOfAttachments);
    }

    void addListToGeneralEmailAttachments(final CaseData caseData,
                                          List<ListValue<DivorceDocument>> list) {
        final GeneralEmail generalEmail = caseData.getGeneralEmail();
        if (isEmpty(generalEmail.getGeneralEmailAttachments())) {
            generalEmail.setGeneralEmailAttachments(list);
        } else {
            list.addAll(generalEmail.getGeneralEmailAttachments());
            generalEmail.setGeneralEmailAttachments(list);
        }
    }
}
