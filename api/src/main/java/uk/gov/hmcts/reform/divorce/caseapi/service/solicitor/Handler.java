package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

public interface Handler<T> {

    T handle(final T instance);
}
