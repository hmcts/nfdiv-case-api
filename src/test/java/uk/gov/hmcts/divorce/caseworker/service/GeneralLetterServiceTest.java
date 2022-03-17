package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateGeneralLetter;
import uk.gov.hmcts.divorce.caseworker.service.task.SendGeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER_ATTACHMENT;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;

@ExtendWith(MockitoExtension.class)
public class GeneralLetterServiceTest {

    private static final LocalDate DATE = LocalDate.of(2022, 3, 16);

    @Mock
    private GenerateGeneralLetter generateGeneralLetter;

    @Mock
    private SendGeneralLetter sendGeneralLetter;

    @Mock
    private Clock clock;

    @InjectMocks
    private GeneralLetterService service;

    @Test
    public void testProcessGeneralLetter() {
        setMockClock(clock, DATE);

        var caseData = buildCaseDataWithGeneralLetter(APPLICANT);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);

        when(generateGeneralLetter.apply(caseDetails)).thenReturn(caseDetails);
        when(sendGeneralLetter.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = service.processGeneralLetter(caseDetails);

        var expectedCaseData = buildCaseDataWithGeneralLetter(APPLICANT);

        List<ListValue<DivorceDocument>> uploadedDocs = new ArrayList<>();

        expectedCaseData.getGeneralLetter().getAttachments().forEach(document -> {
            document.getValue().setDocumentType(GENERAL_LETTER_ATTACHMENT);
            document.getValue().setDocumentDateAdded(DATE);
            uploadedDocs.add(document);
        });

        expectedCaseData.setDocumentsUploaded(uploadedDocs);

        assertThat(response.getData()).isEqualTo(expectedCaseData);

        verify(generateGeneralLetter).apply(caseDetails);
        verify(sendGeneralLetter).apply(caseDetails);
    }
}
