package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ResetAosFieldsTest {

    @InjectMocks
    private ResetAosFields resetAosFields;

    @Test
    void shouldResetAosFieldsIfAwaitingAos() {
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setAcknowledgementOfService(AcknowledgementOfService.builder()
            .confirmReadPetition(YES)
            .aosIsDrafted(YES)
            .statementOfTruth(YES).build());
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseDetails.setState(State.AwaitingAos);

        final CaseDetails<CaseData, State> result = resetAosFields.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService().getConfirmReadPetition()).isNull();
        assertThat(result.getData().getAcknowledgementOfService().getAosIsDrafted()).isNull();
        assertThat(result.getData().getAcknowledgementOfService().getStatementOfTruth()).isNull();
    }

    @Test
    void shouldNotResetAosFieldsIfNotAwaitingAos() {
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setAcknowledgementOfService(AcknowledgementOfService.builder()
            .confirmReadPetition(YES)
            .aosIsDrafted(YES)
            .statementOfTruth(YES).build());
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseDetails.setState(State.Holding);

        final CaseDetails<CaseData, State> result = resetAosFields.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService().getConfirmReadPetition()).isEqualTo(YES);
        assertThat(result.getData().getAcknowledgementOfService().getAosIsDrafted()).isEqualTo(YES);
        assertThat(result.getData().getAcknowledgementOfService().getStatementOfTruth()).isEqualTo(YES);
    }
}
