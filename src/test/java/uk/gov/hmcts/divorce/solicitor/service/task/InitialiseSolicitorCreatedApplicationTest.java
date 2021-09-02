package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class InitialiseSolicitorCreatedApplicationTest {

    @InjectMocks
    private InitialiseSolicitorCreatedApplication initialiseSolicitorCreatedApplication;

    @Test
    void shouldSetApplicationCreatedDateFromCaseDetailsCreatedDateAndApplicant1SolicitorRepresentedToTrue() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = initialiseSolicitorCreatedApplication.apply(caseDetails);

        assertThat(result.getData().getApplication().getCreatedDate()).isEqualTo(LOCAL_DATE);
        assertThat(result.getData().getApplicant1().getSolicitorRepresented()).isEqualTo(YES);
    }
}