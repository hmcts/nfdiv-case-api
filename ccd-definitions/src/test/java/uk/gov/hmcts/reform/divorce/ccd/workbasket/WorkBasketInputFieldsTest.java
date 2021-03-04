package uk.gov.hmcts.reform.divorce.ccd.workbasket;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.types.WorkBasket;
import uk.gov.hmcts.reform.divorce.ccd.mock.WorkBasketBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class WorkBasketInputFieldsTest {

    private final WorkBasketInputFields workBasketInputFields = new WorkBasketInputFields();
    private final WorkBasketBuildingMockUtil workBasketBuildingMockUtil = new WorkBasketBuildingMockUtil().mockWorkBasketBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = workBasketBuildingMockUtil.getConfigBuilder();
    private final WorkBasket.WorkBasketBuilder<CaseData, UserRole> workBasketBuilder = workBasketBuildingMockUtil.getWorkBasketBuilder();

    @Test
    public void shouldBuildWorkBasketInputFieldsWithConfigBuilder() {

        workBasketInputFields.buildWith(configBuilder);

        verify(configBuilder).workBasketInputFields();
        verify(workBasketBuilder).caseReferenceField();
        verifyNoMoreInteractions(configBuilder, workBasketBuilder);
    }
}