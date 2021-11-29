package uk.gov.hmcts.divorce.systemupdate.convert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CaseDetailsListConverterTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private CaseDetailsListConverter caseDetailsListConverter;

    @Test
    void shouldOnlyReturnCasesThatDeserializeWhenConverting() {

        final List<CaseDetails> caseDetailsList = createCaseDetailsList(10);

        final CaseDetails failedCaseDetails1 = caseDetailsList.get(2);
        final CaseDetails failedCaseDetails2 = caseDetailsList.get(8);

        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> expectedResult = new ArrayList<>();

        doAnswer(invocation -> {
            final Object[] arguments = invocation.getArguments();

            if (arguments.length > 0) {
                final CaseDetails details = (CaseDetails) arguments[0];
                if (details.equals(failedCaseDetails1) || details.equals(failedCaseDetails2)) {
                    throw new IllegalArgumentException();
                }
            }

            final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
            expectedResult.add(caseDetails);

            return caseDetails;
        }).when(caseDetailsConverter).convertToCaseDetailsFromReformModel(any(CaseDetails.class));

        final List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>> result =
            caseDetailsListConverter.convertToListOfValidCaseDetails(caseDetailsList);

        assertThat(result)
            .hasSize(8)
            .containsAll(expectedResult);
    }

    private List<CaseDetails> createCaseDetailsList(final int size) {

        final List<CaseDetails> caseDetails = new ArrayList<>();

        for (int index = 0; index < size; index++) {
            caseDetails.add(mock(CaseDetails.class));
        }

        return caseDetails;
    }
}
