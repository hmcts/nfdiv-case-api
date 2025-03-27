package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class MarriageCertificateDetailsTest {

    private final MarriageCertificateDetails page = new MarriageCertificateDetails();

    @Test
    public void shouldSetPlaceOfMarriageToUkIfMarriageTookPlaceInUk() {
        final CaseData caseData = caseData();
        caseData.setApplication(Application.builder()
            .marriageDetails(MarriageDetails.builder()
                .marriedInUk(YES)
                .date(LOCAL_DATE)
                .build())
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertNotNull(response);
        assertEquals("UK",
            response.getData().getApplication().getMarriageDetails().getPlaceOfMarriage()
        );
        assertTrue(response.getErrors() == null || response.getErrors().isEmpty());
    }

    @Test
    public void shouldNotSetPlaceOfMarriageIfMarriageDidNotTakePlaceInUk() {
        final CaseData caseData = caseData();
        caseData.setApplication(Application.builder()
            .marriageDetails(MarriageDetails.builder()
                .marriedInUk(NO)
                .placeOfMarriage("Maldives")
                .build())
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals("Maldives",
            response.getData().getApplication().getMarriageDetails().getPlaceOfMarriage()
        );
        assertNotNull(response);
        assertFalse(response.getErrors().isEmpty());
        assertEquals(caseData, response.getData());
    }

    @Test
    public void shouldCallValidationUtilMethodToValidateMarriageCertificateNames() {
        final CaseData caseData = caseData();
        caseData.setApplication(Application.builder()
            .marriageDetails(MarriageDetails.builder()
                .marriedInUk(NO)
                .date(LocalDate.now().minusYears(1).minusDays(1))
                .placeOfMarriage("Maldives")
                .build())
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        List<String> errors = new ArrayList<>();
        errors.add("Error");

        MockedStatic<ValidationUtil> validationUtilMockedStatic = Mockito.mockStatic(ValidationUtil.class);
        validationUtilMockedStatic.when(() -> ValidationUtil.validateMarriageCertificateNames(caseData)).thenReturn(errors);
        validationUtilMockedStatic.when(() -> ValidationUtil.validateMarriageDate(
            caseData, "MarriageDate")).thenReturn(Collections.emptyList());
        validationUtilMockedStatic.when(() -> ValidationUtil.flattenLists(anyList(), anyList())).thenReturn(errors);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo("Error");

        validationUtilMockedStatic.close();
    }
}
