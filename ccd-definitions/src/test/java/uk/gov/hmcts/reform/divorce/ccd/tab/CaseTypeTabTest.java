package uk.gov.hmcts.reform.divorce.ccd.tab;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.Tab;
import uk.gov.hmcts.reform.divorce.ccd.mock.TabBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CaseTypeTabTest {

    private final CaseTypeTab caseTypeTab = new CaseTypeTab();
    private final TabBuildingMockUtil tabBuildingMockUtil = new TabBuildingMockUtil().mockTabBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = tabBuildingMockUtil.getConfigBuilder();
    private final Tab.TabBuilder<CaseData, UserRole> tabBuilder = tabBuildingMockUtil.getTabBuilder();

    @Test
    public void shouldBuildSearchInputFieldsWithConfigBuilder() {

        caseTypeTab.buildWith(configBuilder);

        verify(configBuilder).tab("petitionDetails", "Petition");
        verify(tabBuilder).field("D8MarriageIsSameSexCouple");
        verify(tabBuilder).field("D8InferredPetitionerGender");
        verify(tabBuilder).field("D8InferredRespondentGender");
        verify(tabBuilder).field("D8MarriageDate");

        verify(configBuilder).tab("paymentDetailsCourtAdmin", "Payment");
        verify(tabBuilder).field("D8HelpWithFeesReferenceNumber");

        verifyNoMoreInteractions(configBuilder, tabBuilder);
    }
}