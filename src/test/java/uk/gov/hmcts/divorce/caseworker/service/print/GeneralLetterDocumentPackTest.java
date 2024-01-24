package uk.gov.hmcts.divorce.caseworker.service.print;


import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.service.print.GeneralLetterDocumentPack.LETTER_TYPE_GENERAL_LETTER;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

class GeneralLetterDocumentPackTest {

    private final GeneralLetterDocumentPack generalLetterDocumentPack = new GeneralLetterDocumentPack();

    @Test
    void shouldReturnApplicant1DocumentPackWhenPassedApplicant1NonJS() {
        CaseData caseData = validApplicant1CaseData();
        var documentPack = generalLetterDocumentPack.getDocumentPack(caseData, null);

        assertThat(documentPack.documentPack()).hasSize(1);
        assertThat(documentPack.documentPack()).containsEntry(DocumentType.GENERAL_LETTER,
                Optional.empty());
    }

    @Test
    public void shouldReturnCorrectLetterId() {
        assertThat(generalLetterDocumentPack.getLetterId()).isEqualTo(LETTER_TYPE_GENERAL_LETTER);
    }
}
