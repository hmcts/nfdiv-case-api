package uk.gov.hmcts.reform.divorce.ccd;

import java.util.stream.Stream;

public interface CcdConfigFactory {

    Stream<CcdConfig> getCcdConfig();
}
