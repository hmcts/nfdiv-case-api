package uk.gov.hmcts.reform.divorce.ccd.mock;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import uk.gov.hmcts.ccd.sdk.types.Search;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class SearchBuilderAnswer implements Answer<Search.SearchBuilder<CaseData, UserRole>> {

    private Search.SearchBuilder<CaseData, UserRole> answer;

    public void setBuilderAnswer(final Search.SearchBuilder<CaseData, UserRole> answer) {
        this.answer = answer;
    }

    @Override
    public Search.SearchBuilder<CaseData, UserRole> answer(InvocationOnMock invocation) throws Throwable {
        return answer;
    }
}
