package uk.gov.hmcts.reform.divorce.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class CcdBuilderApplierTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldApplyCcdBuildersToConfigBuilder() {

        final CcdBuilderFactory ccdBuilderFactory = mock(CcdBuilderFactory.class);
        final ConfigBuilder configBuilder = mock(ConfigBuilder.class);

        final CcdBuilder ccdBuilder1 = mock(CcdBuilder.class);
        final CcdBuilder ccdBuilder2 = mock(CcdBuilder.class);
        final Stream<CcdBuilder> ccdBuilderStream = Stream.of(ccdBuilder1, ccdBuilder2);

        when(ccdBuilderFactory.getCcdBuilders()).thenReturn(ccdBuilderStream);

        new CcdBuilderApplier(ccdBuilderFactory).applyWith(configBuilder);

        verify(ccdBuilderFactory).getCcdBuilders();
        verify(ccdBuilder1).buildWith(configBuilder);
        verify(ccdBuilder2).buildWith(configBuilder);
        verifyNoMoreInteractions(ccdBuilderFactory, configBuilder, ccdBuilder1, ccdBuilder2);
    }
}