package uk.gov.hmcts.reform.divorce.ccd.mock;

import de.cronn.reflection.util.TypedPropertyGetter;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.Tab;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings("rawtypes")
@Getter
public class TabBuildingMockUtil {

    private ConfigBuilder<CaseData, State, UserRole> configBuilder;
    private Tab.TabBuilder<CaseData, UserRole> tabBuilder;
    private Tab.RestrictedFieldBuilder restrictedFieldBuilder;

    @SuppressWarnings("unchecked")
    public TabBuildingMockUtil mockTabBuildingWith(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        this.configBuilder = configBuilder;
        tabBuilder = mock(Tab.TabBuilder.class, withSettings().lenient());
        restrictedFieldBuilder = mock(Tab.RestrictedFieldBuilder.class, withSettings().lenient());

        when(configBuilder.tab(anyString(), anyString())).thenReturn(tabBuilder);

        when(tabBuilder.field(anyString())).thenReturn(tabBuilder);
        when(tabBuilder.field(any(), anyString())).thenReturn(tabBuilder);
        when(tabBuilder.field(any(TypedPropertyGetter.class))).thenReturn(tabBuilder);
        when(tabBuilder.restrictedField(anyString())).thenReturn(restrictedFieldBuilder);
        when(tabBuilder.restrictedField(any(TypedPropertyGetter.class))).thenReturn(restrictedFieldBuilder);
        when(tabBuilder.exclude(any())).thenReturn(tabBuilder);
        when(tabBuilder.collection(any())).thenReturn(tabBuilder);

        return this;
    }

    @SuppressWarnings("unchecked")
    public TabBuildingMockUtil mockTabBuilding() {
        return mockTabBuildingWith(mock(ConfigBuilder.class, withSettings().lenient()));
    }
}
