package uk.gov.hmcts.divorce.document.print.documentpack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD36Form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.document.print.documentpack.ApplyForFinalOrderDocumentPack.APPLICANT1_OFFLINE_DOCUMENTPACK;
import static uk.gov.hmcts.divorce.document.print.documentpack.ApplyForFinalOrderDocumentPack.APPLICANT2_OFFLINE_DOCUMENTPACK;
import static uk.gov.hmcts.divorce.document.print.documentpack.ApplyForFinalOrderDocumentPack.LETTER_TYPE_APPLY_FOR_FINAL_ORDER_PACK;
import static uk.gov.hmcts.divorce.document.print.documentpack.ApplyForFinalOrderDocumentPack.RESPONDENT_OFFLINE_DOCUMENTPACK;

@ExtendWith(MockitoExtension.class)
class ApplyForFinalOrderDocumentPackTest {

    @Mock
    GenerateD36Form generateD36Form;

    @InjectMocks
    ApplyForFinalOrderDocumentPack applyForFinalOrderDocumentPack;

    @Test
    void shouldreturnapplicant1OfflineDocumentpack() {
        Applicant applicant = mock(Applicant.class);
        CaseData caseData = CaseData.builder().applicant1(applicant).build();
        assertEquals(APPLICANT1_OFFLINE_DOCUMENTPACK, applyForFinalOrderDocumentPack.getDocumentPack(caseData, applicant));
        verify(generateD36Form).generateD36Document(caseData);
    }

    @Test
    void shouldreturnapplicant2OfflineDocumentpack() {
        Applicant applicant = mock(Applicant.class);
        CaseData caseData = CaseData.builder().applicant2(applicant).build();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        assertEquals(APPLICANT2_OFFLINE_DOCUMENTPACK, applyForFinalOrderDocumentPack.getDocumentPack(caseData, applicant));
        verify(generateD36Form).generateD36Document(caseData);
    }

    @Test
    void shouldReturnRespondentOfflineDocumentpack() {
        Applicant applicant = mock(Applicant.class);
        CaseData caseData = CaseData.builder().applicant2(applicant).build();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        assertEquals(RESPONDENT_OFFLINE_DOCUMENTPACK, applyForFinalOrderDocumentPack.getDocumentPack(caseData, applicant));
        verify(generateD36Form).generateD36Document(caseData);
    }

    @Test
    void getLetterId() {
        assertEquals(LETTER_TYPE_APPLY_FOR_FINAL_ORDER_PACK, applyForFinalOrderDocumentPack.getLetterId());
    }
}
