package uk.gov.hmcts.divorce.document.print.documentpack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD36Form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.document.print.documentpack.SoleApplicantFinalOrderOverdueDocumentPack.SOLE_APPLICANT_FINAL_ORDER_OVERDUE_DOCUMENT_PACK;
import static uk.gov.hmcts.divorce.document.print.documentpack.SoleApplicantFinalOrderOverdueDocumentPack.SOLE_APPLICANT_FINAL_ORDER_OVERDUE_PACK;

@ExtendWith(MockitoExtension.class)
class SoleApplicantFinalOrderOverdueDocumentPackTest {

    @Mock
    GenerateD36Form generateD36Form;

    @InjectMocks
    SoleApplicantFinalOrderOverdueDocumentPack soleApplicantFinalOrderOverdueDocumentPack;

    @Test
    void shouldReturnDocumentPack() {
        Applicant applicant = mock(Applicant.class);
        CaseData caseData = CaseData.builder().applicant1(applicant).build();
        assertEquals(
            SOLE_APPLICANT_FINAL_ORDER_OVERDUE_DOCUMENT_PACK,
            soleApplicantFinalOrderOverdueDocumentPack.getDocumentPack(caseData, applicant)
        );
        verify(generateD36Form).generateD36Document(caseData);
    }

    @Test
    void getLetterId() {
        assertEquals(SOLE_APPLICANT_FINAL_ORDER_OVERDUE_PACK, soleApplicantFinalOrderOverdueDocumentPack.getLetterId());
    }
}
