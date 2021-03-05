package uk.gov.hmcts.reform.divorce.ccd;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CcdConfigApplierTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldApplyCcdBuildersToConfigBuilder() {

        final CcdConfigFactory ccdConfigFactory = mock(CcdConfigFactory.class);
        final ConfigBuilder configBuilder = mock(ConfigBuilder.class);

        final CcdConfig ccdConfig1 = mock(CcdConfig.class);
        final CcdConfig ccdConfig2 = mock(CcdConfig.class);
        final Stream<CcdConfig> ccdBuilderStream = Stream.of(ccdConfig1, ccdConfig2);

        when(ccdConfigFactory.getCcdConfig()).thenReturn(ccdBuilderStream);

        new CcdConfigApplier(ccdConfigFactory).applyTo(configBuilder);

        verify(ccdConfigFactory).getCcdConfig();
        verify(ccdConfig1).applyTo(configBuilder);
        verify(ccdConfig2).applyTo(configBuilder);
        verifyNoMoreInteractions(ccdConfigFactory, configBuilder, ccdConfig1, ccdConfig2);
    }
}