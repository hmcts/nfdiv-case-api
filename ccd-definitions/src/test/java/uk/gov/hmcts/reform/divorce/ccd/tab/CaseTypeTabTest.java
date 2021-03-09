package uk.gov.hmcts.reform.divorce.ccd.tab;

import de.cronn.reflection.util.TypedPropertyGetter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Tab;
import uk.gov.hmcts.reform.divorce.ccd.mock.TabBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class CaseTypeTabTest {

    private final CaseTypeTab caseTypeTab = new CaseTypeTab();
    private final TabBuildingMockUtil tabBuildingMockUtil = new TabBuildingMockUtil().mockTabBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = tabBuildingMockUtil.getConfigBuilder();
    private final Tab.TabBuilder<CaseData, UserRole> tabBuilder = tabBuildingMockUtil.getTabBuilder();

    @Test
    public void shouldBuildCaseTypeTabWithConfigBuilder() {

        caseTypeTab.applyTo(configBuilder);

        verify(configBuilder).tab("petitionDetails", "Petition");
        verify(configBuilder).tab("paymentDetailsCourtAdmin", "Payment");
    }
}
