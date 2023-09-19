package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.ResolvedCCDConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class BulkActionCaseTypeConfigTest {

    @InjectMocks
    private BulkActionCaseTypeConfig bulkAction;

    @Test
    void shouldSetTheCorrectCaseTypeName() {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = new ConfigBuilderImpl<>(
            new ResolvedCCDConfig<>(
                BulkActionCaseData.class,
                BulkActionState.class,
                UserRole.class,
                new HashMap<>(),
                ImmutableSet.copyOf(BulkActionState.class.getEnumConstants())));

        bulkAction.configure(configBuilder);

        assertThat(configBuilder.build().getCaseDesc()).isEqualTo("Handling of the dissolution of marriage");
    }

}
