package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.print.GeneralLetterDocumentPack;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateGeneralLetter;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
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
public class CaseworkerGeneralLetter implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CREATE_GENERAL_LETTER = "caseworker-create-general-letter";
    private static final String CREATE_GENERAL_LETTER_TITLE = "Create general letter";
    public static final String GENERAL_LETTER_ATTACHMENTS_REQUIRED_ERROR =
        "Please ensure all General Letter attachments have been uploaded before continuing";
    public static final String GENERAL_LETTER_ATTACHMENTS_PDF_ONLY_ERROR =
        "Please only use PDF files as General Letter attachments. Other file types are not supported by the HMCTS bulk print service.";

    private final LetterPrinter letterPrinter;
    private final GeneralLetterDocumentPack generalLetterDocumentPack;
    private final GenerateGeneralLetter generateGeneralLetter;

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
        List<ListValue<DivorceDocument>> letterAttachments = Optional.ofNullable(caseData.getGeneralLetter().getGeneralLetterAttachments())
                .orElse(Collections.emptyList());

        final boolean hasInvalidLetterAttachment = letterAttachments.stream()
            .anyMatch(divorceDocument -> isEmpty(divorceDocument.getValue().getDocumentLink()));
        if (hasInvalidLetterAttachment) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList(GENERAL_LETTER_ATTACHMENTS_REQUIRED_ERROR))
                .build();
        }

        final boolean hasNonPdfLetterAttachment = letterAttachments.stream()
            .map(divorceDocument -> divorceDocument.getValue().getDocumentLink().getFilename())
            .anyMatch(filename -> isEmpty(filename) || !filename.toLowerCase(Locale.ROOT).endsWith(".pdf"));
        if (hasNonPdfLetterAttachment) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList(GENERAL_LETTER_ATTACHMENTS_PDF_ONLY_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();

        log.info("Caseworker create general letter about to submit callback invoked for Case Id: {}", details.getId());

        // Pre-generate letter using existing caseTask to allow letter attachments to be stored on caseData before attempting to send them.
        // XUI now using CDAM and issue with 403 error is resolved now so we can directly
        // generate and send letters from about to submit callback.
        generateGeneralLetter.apply(details);

        Applicant applicant = GeneralParties.APPLICANT.equals(caseData.getGeneralLetter().getGeneralLetterParties())
            ? caseData.getApplicant1()
            : caseData.getApplicant2();

        letterPrinter.sendLetters(caseData,
            details.getId(),
            applicant, // Unused. See above.
            generalLetterDocumentPack.getDocumentPack(caseData, applicant),
            generalLetterDocumentPack.getLetterId());

        details.getData().setGeneralLetter(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }
}
