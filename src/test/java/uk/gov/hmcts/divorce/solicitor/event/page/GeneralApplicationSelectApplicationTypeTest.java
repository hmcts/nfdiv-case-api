package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.event.page.GeneralApplicationSelectApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationSelectApplicationTypeTest {

    private final GeneralApplicationSelectApplicationType page = new GeneralApplicationSelectApplicationType();

    @ParameterizedTest
    @MethodSource("generalApplicationTypeStream")
    void shouldReturnErrorIfServiceApplicationFails(GeneralApplicationType generalApplicationType) {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationType(generalApplicationType).build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(1, response.getErrors().size());
        assertThat(response.getErrors()).containsExactly("The selected application type is a Service Application and cannot be "
            + "processed through the General Application event. Please use the appropriate Service Application event to continue.");
    }

    private static Stream<GeneralApplicationType> generalApplicationTypeStream() {
        return Stream.of(GeneralApplicationType.DEEMED_SERVICE,GeneralApplicationType.DISPENSED_WITH_SERVICE,
            GeneralApplicationType.OTHER_ALTERNATIVE_SERVICE_METHODS);
    }

    @Test
    void shouldNotReturnErrorForOtherGeneralApplicationType() {
        final CaseData caseData = caseData();
        caseData.setGeneralApplication(GeneralApplication.builder()
            .generalApplicationType(GeneralApplicationType.AMEND_APPLICATION).build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertNull(response.getErrors());
    }
}
