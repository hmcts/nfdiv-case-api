package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

@Component
@Slf4j
public class UploadGeneralLetterAttachments implements CaseTask {

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        log.info("Uploading general letter attachments for case id: {}", caseDetails.getId());

        CaseData caseData = caseDetails.getData();

        if (!CollectionUtils.isEmpty(caseData.getGeneralLetter().getGeneralLetterAttachments())) {
            LocalDateTime createdDate = now(clock);

            caseData.getGeneralLetter().getGeneralLetterAttachments()
                .forEach(document -> {
                    DivorceDocument divorceDocument = document.getValue();

                    if (divorceDocument.getDocumentDateAdded() == null) {
                        divorceDocument.setDocumentDateAdded(createdDate.toLocalDate());
                    }

                    caseData.addToDocumentsUploaded(document);
                });
        }

        return caseDetails;
    }
}
