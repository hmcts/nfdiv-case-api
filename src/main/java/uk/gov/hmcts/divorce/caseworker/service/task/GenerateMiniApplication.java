package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.MiniApplicationTemplateContent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Supplier;

import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_APPLICATION_DOCUMENT_NAME;
<<<<<<< HEAD
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
=======
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.FormatUtil.FILE_NAME_DATE_TIME_FORMATTER;
>>>>>>> Updated reissue application service and added unit tests

@Component
@Slf4j
public class GenerateMiniApplication implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private MiniApplicationTemplateContent templateContent;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final LocalDate createdDate = caseDetails.getCreatedDate().toLocalDate();

        log.info("Executing handler for generating mini application for case id {} ", caseId);

        final Supplier<Map<String, Object>> templateContentSupplier = templateContent.apply(caseData, caseId, createdDate);

        String filename = new StringJoiner("-")
            .add(DIVORCE_MINI_APPLICATION_DOCUMENT_NAME)
            .add(String.valueOf(caseId))
            .add(LocalDateTime.now(clock).format(FILE_NAME_DATE_TIME_FORMATTER))
            .toString();

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            APPLICATION,
            templateContentSupplier,
            caseId,
            DIVORCE_MINI_APPLICATION,
            caseData.getApplicant1().getLanguagePreference(),
            filename
        );

        return caseDetails;
    }
}
