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
class SolPaymentTest {

    private final FieldCollectionBuilder fieldCollectionBuilder =
        new EventBuildingMockUtil().mockEventBuilding().getFieldCollectionBuilder();

    @InjectMocks
    private SolPayment solPayment;

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddSolPaymentPageConfiguration() {

        solPayment.addTo(fieldCollectionBuilder);

        verify(fieldCollectionBuilder).page("SolPayment");
        verify(fieldCollectionBuilder).pageLabel("Payment");
        verify(fieldCollectionBuilder).label(
            "LabelSolPaymentPara-1",
            "Amount to pay: **£${SolApplicationFeeInPounds}**");
        verify(fieldCollectionBuilder).mandatory(any(TypedPropertyGetter.class));

        verifyNoMoreInteractions(fieldCollectionBuilder);
    }
}