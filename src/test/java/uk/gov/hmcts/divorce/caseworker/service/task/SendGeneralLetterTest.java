package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.print.GeneralLetterPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;

@ExtendWith(MockitoExtension.class)
public class SendGeneralLetterTest {
    @Mock
    private GeneralLetterPrinter printer;

    @InjectMocks
    private SendGeneralLetter sendGeneralLetter;

    @Test
    public void testSendLetter() {
        CaseData caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final var result = sendGeneralLetter.apply(caseDetails);

        verify(printer).sendLetterWithAttachments(caseData, TEST_CASE_ID);

        assertThat(result.getData()).isEqualTo(caseData);
    }
}
