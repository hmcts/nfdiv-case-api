package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.populateGeneralApplicationList;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithMarriageDate;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationUtilsTest {

    @Test
    void shouldHandleNullGeneralApplicationsWhenSettingLabels() {
        final CaseData caseData = caseDataWithMarriageDate();
        caseData.setGeneralApplications(null);

        populateGeneralApplicationList(caseData);
        assertThat(caseData.getGeneralReferral().getSelectedGeneralApplication().getListItems()).isEmpty();
    }

    @Test
    void shouldPopulateGeneralApplicationList() {
        final CaseData caseData = caseDataWithMarriageDate();
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        caseData.setGeneralApplications(generalApplications);

        populateGeneralApplicationList(caseData);

        assertThat(caseData.getGeneralReferral().getSelectedGeneralApplication().getListItems()).hasSize(2);
    }

    private List<ListValue<GeneralApplication>> buildListOfGeneralApplications() {
        return List.of(
            ListValue.<GeneralApplication>builder().value(
                GeneralApplication.builder()
                    .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
                    .generalApplicationSubmittedOnline(YesOrNo.YES)
                    .generalApplicationFee(
                        FeeDetails.builder()
                            .serviceRequestReference(TEST_SERVICE_REFERENCE)
                            .paymentReference(TEST_SERVICE_REFERENCE)
                            .build()
                    )
                    .build()
            ).build(),
            ListValue.<GeneralApplication>builder().value(
                GeneralApplication.builder()
                    .generalApplicationType(GeneralApplicationType.DISCLOSURE_VIA_DWP)
                    .generalApplicationSubmittedOnline(YesOrNo.YES)
                    .generalApplicationParty(GeneralParties.APPLICANT)
                    .generalApplicationFee(
                        FeeDetails.builder()
                            .serviceRequestReference(TEST_SERVICE_REFERENCE)
                            .build()
                    )
                    .generalApplicationReceivedDate(LocalDateTime.of(2022, 1, 1, 1, 1, 1))
                    .build()
            ).build()
        );
    }
}
