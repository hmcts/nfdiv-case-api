package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@Component
@Slf4j
public class UploadGeneralLetterAttachments implements CaseTask {

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        log.info("Uploading general letter attachments for case id: {}", caseDetails.getId());

        CaseData caseData = caseDetails.getData();

        caseData.getGeneralLetter().getAttachments()
            .forEach(document ->
                caseData.addToDocumentsUploaded(mapToDivorceDocument(document)));

        return caseDetails;
    }

    private ListValue<DivorceDocument> mapToDivorceDocument(final Document document) {
        return ListValue.<DivorceDocument>builder()
            .id(String.valueOf(UUID.randomUUID()))
            .value(DivorceDocument.builder()
                .documentDateAdded(LocalDate.now(clock))
                .documentFileName(document.getFilename())
                .documentLink(document)
                .documentComment("General letter attachment")
                .documentType(DocumentType.OTHER).build())
            .build();
    }
}
