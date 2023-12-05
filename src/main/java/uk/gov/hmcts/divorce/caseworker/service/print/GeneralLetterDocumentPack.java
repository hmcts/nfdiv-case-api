package uk.gov.hmcts.divorce.caseworker.service.print;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPack;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralLetterDocumentPack implements DocumentPack {

    private static final String LETTER_TYPE_GENERAL_LETTER = "general-letter";

    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant) {

        return new DocumentPackInfo(ImmutableMap.of(GENERAL_LETTER, Optional.of(GENERAL_LETTER_TEMPLATE_ID)),
        ImmutableMap.of(GENERAL_LETTER_TEMPLATE_ID, GENERAL_LETTER_DOCUMENT_NAME));
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_GENERAL_LETTER;
    }
}
