package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.caseworker.service.notification.PaperApplicationReceivedNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.content.DocmosisTemplateProvider;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Withdrawn;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;

import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.PAPER_APPLICATION_RECEIVED_TEMPLATE_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseworkerBulkSendLetters implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_SEND_LETTERS = "caseworker-send-letters";
    private static final String LETTER_SENT = "sentLetters";

    private final BulkPrintService bulkPrintService;
    private final CaseDataDocumentService caseDataDocumentService;
    private final DocmosisCommonContent templateContent;
    private final DocmosisTemplateProvider docmosisTemplateProvider;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_SEND_LETTERS)
            .forStates(POST_SUBMISSION_STATES)
            .name("Send letters")
            .description("Send letters")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER)
            .aboutToSubmitCallback(this::aboutToSubmit));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

       generateLetter(details.getId(), details.getData().getApplicant1());


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }

    private void generateLetter(Long caseId, Applicant applicant) {

        List.of(
            "NFD_APPLICANT_PAPERCASE_RECEIVED",
            "RESPONDENT_DRAFT_AOS_STARTED_APPLICATION",
            "NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1__APP2",
            "_NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED",
                "_NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1__APP2",
                "_NFD_APP1_SOLICITOR_APPLIED_FOR_FINAL_ORDER",
                "_NFD_APPLICANT_PAPERCASE_RECEIVED",
                "_NFD_NOTICE_OF_CHANGE_INVITE_APP",
                "_NFD_SOL_STOPPED_REP_NOTIFY_APP",
                "_REQUEST_FOR_INFORMATION_LETTER",
                "_REQUEST_FOR_INFORMATION_SOLICITOR_LETTER",
                "_REQUEST_FOR_INFORMATION_RESPONSE_LETTER",
                "_REQUEST_FOR_INFORMATION_SOLICITOR_RESPONSE_LETTER",
                "_REQUEST_FOR_INFORMATION_PARTNER_RESPONSE_LETTER",
                "_REQUEST_FOR_INFORMATION_PARTNER_SOLICITOR_RESPONSE_LETTER",
                "_RESPONDENT_DRAFT_AOS_STARTED_APPLICATION",
                "_DEEMED_SERVICE_APPLICATION",
                "_BAILIFF_SERVICE_APPLICATION",
                "_ALTERNATIVE_SERVICE_APPLICATION",
                "_DISPENSE_WITH_SERVICE_APPLICATION",
                "_SEARCH_GOV_RECORDS_APPLICATION"
        ).forEach(templateId -> {
            LanguagePreference languagePreference = LanguagePreference.ENGLISH;
            if (templateId.startsWith("_")) {
                languagePreference = LanguagePreference.WELSH;
                templateId = templateId.substring(1);
            }

            Letter letter = new Letter(generateDocument(caseId, languagePreference, templateId), 1);
            String caseIdString = String.valueOf(caseId);

            final Print print = new Print(
                List.of(letter),
                caseIdString,
                caseIdString,
                LETTER_SENT,
                applicant.getFullName(),
                applicant.getAddressOverseas()
            );

            final UUID letterId = bulkPrintService.print(print);

            log.info("Letter service responded with letter Id {} for template id {} langugae preference {}", letterId, templateId, languagePreference);
        });
    }

    private Document generateDocument(final long caseId,
                                      LanguagePreference languagePreference,
                                      String templateId
    ) {

        return caseDataDocumentService.renderDocument(templateContent.getBasicDocmosisTemplateContent(languagePreference),
            caseId,
            templateId,
           languagePreference,
            LETTER_SENT);
    }
}
