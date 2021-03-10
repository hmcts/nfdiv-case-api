package uk.gov.hmcts.reform.divorce.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcdConfigApplierTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldApplyCcdBuildersToConfigBuilder() {

        final CcdConfigFactory ccdConfigFactory = mock(CcdConfigFactory.class);
        final ConfigBuilder configBuilder = mock(ConfigBuilder.class);

        final CcdConfiguration ccdConfig1 = mock(CcdConfiguration.class);
        final CcdConfiguration ccdConfig2 = mock(CcdConfiguration.class);
        final Stream<CcdConfiguration> ccdBuilderStream = Stream.of(ccdConfig1, ccdConfig2);

        when(ccdConfigFactory.getCcdConfig()).thenReturn(ccdBuilderStream);

        new CcdConfigApplier(ccdConfigFactory).applyTo(configBuilder);

        verify(ccdConfigFactory).getCcdConfig();
        verify(ccdConfig1).applyTo(configBuilder);
        verify(ccdConfig2).applyTo(configBuilder);
        verifyNoMoreInteractions(ccdConfigFactory, configBuilder, ccdConfig1, ccdConfig2);
    }
}
