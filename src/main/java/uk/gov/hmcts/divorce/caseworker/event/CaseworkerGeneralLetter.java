package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.print.GeneralLetterDocumentPack;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static java.util.stream.Stream.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getConfidentialDocumentType;
import static uk.gov.hmcts.divorce.document.DocumentUtil.isConfidential;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerGeneralLetter implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CREATE_GENERAL_LETTER = "caseworker-create-general-letter";
    private static final String CREATE_GENERAL_LETTER_TITLE = "Create general letter";

    private final LetterPrinter letterPrinter;
    private final GeneralLetterDocumentPack generalLetterDocumentPack;
    private final Clock clock;
    private final DocumentIdProvider documentIdProvider;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_CREATE_GENERAL_LETTER)
            .forStates(POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED)
            .name(CREATE_GENERAL_LETTER_TITLE)
            .description(CREATE_GENERAL_LETTER_TITLE)
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, SOLICITOR, JUDGE, CITIZEN))
            .page("createGeneralLetter", this::midEvent)
            .pageLabel(CREATE_GENERAL_LETTER_TITLE)
            .complex(CaseData::getGeneralLetter)
                .mandatory(GeneralLetter::getGeneralLetterParties)
                .mandatory(GeneralLetter::getOtherRecipientName, "generalLetterParties=\"other\"")
                .mandatory(GeneralLetter::getOtherRecipientAddress, "generalLetterParties=\"other\"")
                .mandatory(GeneralLetter::getGeneralLetterDetails)
                .optional(GeneralLetter::getGeneralLetterAttachments)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(final CaseDetails<CaseData, State> details,
                                                                  final CaseDetails<CaseData, State> detailsBefore) {

        final CaseData caseData = details.getData();
        final boolean invalidGeneralLetterAttachments = ofNullable(caseData.getGeneralLetter().getGeneralLetterAttachments())
            .flatMap(Collection::stream)
            .anyMatch(divorceDocument -> isEmpty(divorceDocument.getValue().getDocumentLink()));

        if (invalidGeneralLetterAttachments) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("Please ensure all General Letter attachments have been uploaded before continuing"))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();

        log.info("Caseworker create general letter about to submit callback invoked for Case Id: {}", details.getId());

        Applicant applicant = GeneralParties.RESPONDENT.equals(caseData.getGeneralLetter().getGeneralLetterParties())
                ? caseData.getApplicant2()
                : caseData.getApplicant1();

        letterPrinter.sendLetters(caseData,
                details.getId(),
                applicant,
                generalLetterDocumentPack.getDocumentPack(caseData, applicant),
                generalLetterDocumentPack.getLetterId());

        updateGeneralLetters(caseData);

        //clear general letter field so that on next general letter old data is not shown
        details.getData().setGeneralLetter(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    private void updateGeneralLetters(CaseData caseData) {
        Optional<Document> generalLetterDocument;
        if (isConfidential(caseData, GENERAL_LETTER)) {
            generalLetterDocument =
                    ofNullable(caseData.getDocuments().getConfidentialDocumentsGenerated())
                            .flatMap(Collection::stream)
                            .map(ListValue::getValue)
                            .filter(document -> getConfidentialDocumentType(GENERAL_LETTER)
                                    .equals(document.getConfidentialDocumentsReceived()))
                            .findFirst()
                            .map(ConfidentialDivorceDocument::getDocumentLink);
        } else {
            generalLetterDocument =
                    ofNullable(caseData.getDocuments().getDocumentsGenerated())
                            .flatMap(Collection::stream)
                            .map(ListValue::getValue)
                            .filter(document -> GENERAL_LETTER.equals(document.getDocumentType()))
                            .findFirst()
                            .map(DivorceDocument::getDocumentLink);
        }

        generalLetterDocument.ifPresent(document -> caseData.setGeneralLetters(addDocumentToTop(
                caseData.getGeneralLetters(),
                mapToGeneralLetterDetails(caseData.getGeneralLetter(), document),
                documentIdProvider.documentId()
        )));
    }

    private GeneralLetterDetails mapToGeneralLetterDetails(GeneralLetter generalLetter, Document generalLetterDocument) {

        List<ListValue<Document>> attachments = ofNullable(generalLetter.getGeneralLetterAttachments())
                .flatMap(Collection::stream)
                .map(divorceDocument -> ListValue.<Document>builder()
                        .id(documentIdProvider.documentId())
                        .value(divorceDocument.getValue().getDocumentLink()).build())
                .toList();

        return GeneralLetterDetails.builder()
                .generalLetterLink(generalLetterDocument)
                .generalLetterAttachmentLinks(attachments)
                .generalLetterDateTime(now(clock))
                .generalLetterParties(generalLetter.getGeneralLetterParties())
                .build();
    }
}
