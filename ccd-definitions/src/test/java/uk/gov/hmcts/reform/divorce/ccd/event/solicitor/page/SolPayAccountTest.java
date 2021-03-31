package uk.gov.hmcts.reform.divorce.ccd.event.solicitor.page;

import de.cronn.reflection.util.TypedPropertyGetter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("rawtypes")
@ExtendWith(MockitoExtension.class)
class SolPayAccountTest {

    private final FieldCollectionBuilder fieldCollectionBuilder =
        new EventBuildingMockUtil().mockEventBuilding().getFieldCollectionBuilder();

    @InjectMocks
    private SolPayAccount solPayAccount;

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddSolPayAccountPageConfiguration() {

        solPayAccount.addTo(fieldCollectionBuilder);

        verify(fieldCollectionBuilder).page("SolPayAccount");
        verify(fieldCollectionBuilder).pageLabel("Pay account");
        verify(fieldCollectionBuilder).showCondition("solPaymentHowToPay=\"feePayByAccount\"");
        verify(fieldCollectionBuilder, times(2))
            .mandatory(any(TypedPropertyGetter.class));

        verifyNoMoreInteractions(fieldCollectionBuilder);
    }
}
