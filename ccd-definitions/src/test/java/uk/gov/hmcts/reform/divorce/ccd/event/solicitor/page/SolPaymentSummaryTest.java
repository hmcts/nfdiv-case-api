package uk.gov.hmcts.reform.divorce.ccd.event.solicitor.page;

import de.cronn.reflection.util.TypedPropertyGetter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("rawtypes")
@ExtendWith(MockitoExtension.class)
class SolPaymentSummaryTest {

    private final FieldCollectionBuilder fieldCollectionBuilder =
        new EventBuildingMockUtil().mockEventBuilding().getFieldCollectionBuilder();

    @InjectMocks
    private SolPaymentSummary solPaymentSummary;

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddSolPaymentSummaryPageConfiguration() {

        solPaymentSummary.addTo(fieldCollectionBuilder);

        verify(fieldCollectionBuilder).page("SolPaymentSummary");
        verify(fieldCollectionBuilder).mandatory(any(TypedPropertyGetter.class));
        verify(fieldCollectionBuilder).label(
            "LabelSolPaySummaryFeeAccountPara-1",
            "Payment Method: Fee Account",
            "SolPaymentHowToPay=\"feePayByAccount\"");
        verify(fieldCollectionBuilder).label(
            "LabelSolicitorReference",
            "Your fee account reference: **${FeeAccountReference}**",
            "SolPaymentHowToPay=\"feePayByAccount\"");
        verify(fieldCollectionBuilder).label(
            "LabelSolPaySummaryHWFPara-1",
            "Payment Method: Help with fees",
            "SolPaymentHowToPay=\"feesHelpWith\"");
        verify(fieldCollectionBuilder).label(
            "LabelHelpWithFeesReferenceNumber",
            "Help with fee reference: **${D8HelpWithFeesReferenceNumber}**",
            "SolPaymentHowToPay=\"feesHelpWith\"");

        verifyNoMoreInteractions(fieldCollectionBuilder);
    }
}