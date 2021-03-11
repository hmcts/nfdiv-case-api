package uk.gov.hmcts.reform.divorce.ccd.workbasket;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.WorkBasket;
import uk.gov.hmcts.reform.divorce.ccd.mock.WorkBasketBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import de.cronn.reflection.util.TypedPropertyGetter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class WorkBasketSearchTabTest {

    private final WorkBasketSearchTab workBasketSearchTab = new WorkBasketSearchTab();

    private final WorkBasketBuildingMockUtil workBasketBuildingMockUtil =
        new WorkBasketBuildingMockUtil().mockWorkBasketBuilding();

    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = workBasketBuildingMockUtil.getConfigBuilder();

    private final WorkBasket.WorkBasketBuilder<CaseData, UserRole> workBasketBuilder =
        workBasketBuildingMockUtil.getWorkBasketBuilder();

    @Test
    void shouldBuildWorkBasketSearchTabWithConfigBuilder() {
        workBasketSearchTab.applyTo(configBuilder);

        verify(configBuilder).workBasketResultFields();
        verify(workBasketBuilder).field(any(TypedPropertyGetter.class),Mockito.eq("FirstName"));
        verify(workBasketBuilder).field(any(TypedPropertyGetter.class),Mockito.eq("LastName"));
        verify(workBasketBuilder).field(any(TypedPropertyGetter.class),Mockito.eq("Email"));
        verifyNoMoreInteractions(workBasketBuilder,configBuilder);
    }
}
