package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;

@ExtendWith(MockitoExtension.class)
public class UploadGeneralLetterAttachmentsTest {

    private static final LocalDate DATE = LocalDate.of(2022, 3, 16);

    @Mock
    private Clock clock;

    @InjectMocks
    private UploadGeneralLetterAttachments uploadGeneralLetterAttachments;

    @Test
    public void shouldUploadAttachmentsToCaseData() {
        setMockClock(clock, DATE);

        var caseData = buildCaseDataWithGeneralLetter(APPLICANT);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);

        final CaseDetails<CaseData, State> response = uploadGeneralLetterAttachments.apply(caseDetails);

        DivorceDocument attachment = DivorceDocument.builder()
            .documentFileName("some-file")
            .documentDateAdded(DATE)
            .build();

        assertThat(response.getData().getDocumentsUploaded().size()).isEqualTo(1);
        assertThat(response.getData().getDocumentsUploaded().get(0).getValue())
            .isEqualTo(attachment);
    }
}
