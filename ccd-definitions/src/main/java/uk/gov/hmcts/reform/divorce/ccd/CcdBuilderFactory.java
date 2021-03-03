package uk.gov.hmcts.reform.divorce.ccd;

import java.util.stream.Stream;

public interface CcdBuilderFactory {

    Stream<CcdBuilder> getCcdBuilders();
}
