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
class HelpWithFeesTest {

    private final FieldCollectionBuilder fieldCollectionBuilder =
        new EventBuildingMockUtil().mockEventBuilding().getFieldCollectionBuilder();

    @InjectMocks
    private HelpWithFees helpWithFees;

    @SuppressWarnings("unchecked")
    @Test
    void shouldAddHelpWithFeesPageConfiguration() {

        helpWithFees.addTo(fieldCollectionBuilder);

        verify(fieldCollectionBuilder).page("HelpWithFees");
        verify(fieldCollectionBuilder).pageLabel("Help with fees");
        verify(fieldCollectionBuilder).showCondition("solPaymentHowToPay=\"feesHelpWith\"");
        verify(fieldCollectionBuilder).mandatory(any(TypedPropertyGetter.class));

        verifyNoMoreInteractions(fieldCollectionBuilder);
    }
}
