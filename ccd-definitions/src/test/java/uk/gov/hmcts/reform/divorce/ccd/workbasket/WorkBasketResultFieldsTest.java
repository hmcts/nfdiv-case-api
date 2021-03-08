package uk.gov.hmcts.reform.divorce.ccd.workbasket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.WorkBasket;
import uk.gov.hmcts.reform.divorce.ccd.mock.WorkBasketBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class WorkBasketResultFieldsTest {

    private final WorkBasketResultFields workBasketResultFields = new WorkBasketResultFields();
    private final WorkBasketBuildingMockUtil workBasketBuildingMockUtil = new WorkBasketBuildingMockUtil().mockWorkBasketBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = workBasketBuildingMockUtil.getConfigBuilder();
    private final WorkBasket.WorkBasketBuilder<CaseData, UserRole> workBasketBuilder = workBasketBuildingMockUtil.getWorkBasketBuilder();

    @Test
    public void shouldBuildWorkBasketResultFieldsWithConfigBuilder() {

        workBasketResultFields.applyTo(configBuilder);

        verify(configBuilder).workBasketResultFields();
        verify(workBasketBuilder).caseReferenceField();
        verifyNoMoreInteractions(configBuilder, workBasketBuilder);
    }
}
