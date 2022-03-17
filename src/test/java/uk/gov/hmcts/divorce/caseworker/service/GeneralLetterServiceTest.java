package uk.gov.hmcts.divorce.caseworker.service;

import com.google.common.collect.Lists;
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
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;

@ExtendWith(MockitoExtension.class)
public class GeneralLetterServiceTest {

    @Mock
    private GenerateGeneralLetter generateGeneralLetter;

    @Mock
    private SendGeneralLetter sendGeneralLetter;

    @InjectMocks
    private GeneralLetterService service;

    @Test
    public void testProcessGeneralLetter() {
        var caseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);

        when(generateGeneralLetter.apply(caseDetails)).thenReturn(caseDetails);
        when(sendGeneralLetter.apply(caseDetails)).thenReturn(caseDetails);

        final CaseDetails<CaseData, State> response = service.processGeneralLetter(caseDetails);

        var expectedCaseData = buildCaseDataWithGeneralLetter(GeneralParties.APPLICANT);
        List<ListValue<DivorceDocument>> attachments = Lists.newArrayList(caseData.getGeneralLetter().getAttachments());
        expectedCaseData.setDocumentsUploaded(attachments);

        assertThat(response.getData()).isEqualTo(expectedCaseData);

        verify(generateGeneralLetter).apply(caseDetails);
        verify(sendGeneralLetter).apply(caseDetails);
    }
}
