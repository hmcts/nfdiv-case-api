package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CitizenRespondentAosInvitationTemplateContentTest {

    @Mock
    private NoticeOfProceedingContent noticeOfProceedingContent;

    @InjectMocks
    private CitizenRespondentAosInvitationTemplateContent citizenRespondentAosInvitationTemplateContent;

    @Test
    void shouldReturnRespondentAosInvitationTemplateContent() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant2().setAddress(
            AddressGlobalUK.builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .postTown("city")
                .postCode("postcode")
                .build()

        );
        final Map<String, Object> templateContent = new HashMap<>();

        when(noticeOfProceedingContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        final Map<String, Object> result = citizenRespondentAosInvitationTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result).isSameAs(templateContent);
    }
}
