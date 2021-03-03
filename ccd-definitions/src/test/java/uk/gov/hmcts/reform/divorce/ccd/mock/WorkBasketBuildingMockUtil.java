package uk.gov.hmcts.reform.divorce.ccd.mock;

import de.cronn.reflection.util.TypedPropertyGetter;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.WorkBasket;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Getter
public class WorkBasketBuildingMockUtil {

    private ConfigBuilder<CaseData, State, UserRole> configBuilder;
    private WorkBasket.WorkBasketBuilder<CaseData, UserRole> workBasketBuilder;

    @SuppressWarnings("unchecked")
    public WorkBasketBuildingMockUtil mockWorkBasketBuilding() {

        configBuilder = mock(ConfigBuilder.class);
        workBasketBuilder = mock(WorkBasket.WorkBasketBuilder.class);

        when(configBuilder.workBasketInputFields()).thenReturn(workBasketBuilder);
        when(configBuilder.workBasketResultFields()).thenReturn(workBasketBuilder);

        when(workBasketBuilder.field(any(TypedPropertyGetter.class), anyString())).thenReturn(workBasketBuilder);
        when(workBasketBuilder.field(anyString(), anyString())).thenReturn(workBasketBuilder);
        when(workBasketBuilder.caseReferenceField()).thenReturn(workBasketBuilder);
        when(workBasketBuilder.stateField()).thenReturn(workBasketBuilder);
        when(workBasketBuilder.createdDateField()).thenReturn(workBasketBuilder);

        return this;
    }
}
