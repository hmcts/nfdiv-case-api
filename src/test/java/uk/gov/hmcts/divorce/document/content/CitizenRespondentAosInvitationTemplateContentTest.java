package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.testutil.TestConstants.LINE_1_LINE_2_CITY_POSTCODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CitizenRespondentAosInvitationTemplateContentTest {

    @Mock
    private RespondentSolicitorAosInvitationTemplateContent respondentSolicitorAosInvitationTemplateContent;

    @InjectMocks
    private CitizenRespondentAosInvitationTemplateContent citizenRespondentAosInvitationTemplateContent;

    @Test
    void shouldReturnRespondentSolicitorAosInvitationTemplateContent() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder().address(LINE_1_LINE_2_CITY_POSTCODE).build()
        );
        final Map<String, Object> templateContent = new HashMap<>();

        when(respondentSolicitorAosInvitationTemplateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE)).thenReturn(templateContent);

        final Map<String, Object> result = citizenRespondentAosInvitationTemplateContent.apply(caseData, TEST_CASE_ID, LOCAL_DATE);

        assertThat(result).isSameAs(templateContent);
    }
}
