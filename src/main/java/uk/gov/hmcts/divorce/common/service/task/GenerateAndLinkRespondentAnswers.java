package uk.gov.hmcts.divorce.common.service.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.Collection;
import java.util.stream.Stream;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;

@Component
@RequiredArgsConstructor
public class GenerateAndLinkRespondentAnswers implements CaseTask {

    private final DocumentGenerator documentGenerator;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {
        final CaseData caseData = caseDetails.getData();
        final var haveReceivedOfflineRespAnswers = emptyIfNull(caseData.getDocuments().getDocumentsUploaded()).stream()
            .anyMatch(divorceDocumentListValue -> RESPONDENT_ANSWERS.equals(divorceDocumentListValue.getValue().getDocumentType()));

        if (!haveReceivedOfflineRespAnswers || !isEmpty(caseData.getAcknowledgementOfService().getDateAosSubmitted())) {
            documentGenerator.generateAndStoreCaseDocument(
                RESPONDENT_ANSWERS,
                RESPONDENT_ANSWERS_TEMPLATE_ID,
                RESPONDENT_ANSWERS_DOCUMENT_NAME,
                caseData,
                caseDetails.getId()
            );
        }

        Stream.ofNullable(caseData.getDocuments().getDocumentsGenerated())
            .flatMap(Collection::stream)
            .map(ListValue::getValue)
            .filter(divorceDocument ->
                RESPONDENT_ANSWERS.equals(divorceDocument.getDocumentType()))
            .map(DivorceDocument::getDocumentLink)
            .findFirst()
            .ifPresent(file -> caseData.getConditionalOrder().setRespondentAnswersLink(file));

        return caseDetails;
    }
}
