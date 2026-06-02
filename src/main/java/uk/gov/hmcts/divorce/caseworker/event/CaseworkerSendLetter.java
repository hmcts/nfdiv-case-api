package uk.gov.hmcts.divorce.caseworker.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.print.GeneralLetterDocumentPack;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateGeneralLetter;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.*;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static java.util.stream.Stream.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.*;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.*;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.OTHER;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerSendLetter implements CCDConfig<CaseData, State, UserRole> {

    public static final String GENERATE_TEST_DOCUMENT = "generate-test-document";
    private static final String GENERATE_TEST_DOCUMENT_TITLE = "_Generate Test Document";

    private final CaseDataDocumentService caseDataDocumentService;
    private final Clock clock;
    private final DocmosisCommonContent docmosisCommonContent;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(GENERATE_TEST_DOCUMENT)
            .forAllStates()
            .name(GENERATE_TEST_DOCUMENT_TITLE)
            .description(GENERATE_TEST_DOCUMENT_TITLE)
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly());
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();

        log.info("Caseworker create general letter about to submit callback invoked for Case Id: {}", details.getId());

        String templateId = "NFD_NOP_APP1_JS_SOLE_DISPUTED";
        LanguagePreference languagePreference = LanguagePreference.ENGLISH;
        //Map<String, Object> templateContent = getTemplateContent(caseData, details.getId(), languagePreference);
        Map<String, Object> templateContent = null;
        try {
            templateContent = getTemplateContentFromJsonString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        var generatedDocument = caseDataDocumentService.renderDocument(templateContent,
            details.getId(),
            templateId,
            languagePreference,
            formatDocumentName(details.getId(), "Test document with family court logo", now(clock)));

        caseDataDocumentService.updateCaseData(caseData, OTHER, generatedDocument, details.getId(), templateId);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    private Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, LanguagePreference languagePreference) {
        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(languagePreference);

        Applicant applicant = caseData.getApplicant1();
        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());

        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.putAll(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, caseId, true ,languagePreference));

        return templateContent;
    }

    private Map<String, Object> getTemplateContentFromJsonString() throws JsonProcessingException {
        String jsonString = """
  {
  "firstName": "value1",
  "lastName": "value2",
  "address": "value3",
  "divorceAndDissolutionHeader": "value4",
  "courtsAndTribunalsServiceHeader": "value5",
  "ctscContactDetails": {
    "poBox": "value6",
    "town": "value7",
    "postcode": "value8"
  },
  "date": "value9",
  "caseReference": "value10",
  "isJoint": "value11",
  "contactEmail": "value12",
  "phoneAndOpeningTimes": "value13"
  }
                """;

        ObjectMapper mapper = new ObjectMapper();

        // Parses any JSON structure dynamically
        Map<String, Object> map = mapper.readValue(jsonString, new TypeReference<>() {
        });
        return map;
    }
}
