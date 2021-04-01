package uk.gov.hmcts.reform.divorce.caseapi.util;

public interface Handler<T> {

    void handle(final T instance);
}
