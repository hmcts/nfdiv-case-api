package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniApplicationDraft;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniApplicationRemover;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;

@ExtendWith(MockitoExtension.class)
class SolicitorUpdateApplicationServiceTest {

    @Mock
    private MiniApplicationRemover miniApplicationRemover;

    @Mock
    private MiniApplicationDraft miniApplicationDraft;

    @Mock
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @InjectMocks
    private SolicitorUpdateApplicationService solicitorUpdateApplicationService;

    @Test
    void shouldCompleteStepsToUpdateApplication() {

        final CaseData caseData = mock(CaseData.class);
        final CaseDataUpdaterChain caseDataUpdaterChain = mock(CaseDataUpdaterChain.class);

        final List<CaseDataUpdater> caseDataUpdaters = asList(
            miniApplicationRemover,
            miniApplicationDraft
        );

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .createdDate(LOCAL_DATE)
            .userAuthToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(caseDataUpdaterChainFactory.createWith(caseDataUpdaters)).thenReturn(caseDataUpdaterChain);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);

        final CaseData actualCaseData = solicitorUpdateApplicationService.aboutToSubmit(
            caseData,
            TEST_CASE_ID,
            LOCAL_DATE,
            TEST_AUTHORIZATION_TOKEN
        );

        assertThat(actualCaseData, is(caseData));
    }
}