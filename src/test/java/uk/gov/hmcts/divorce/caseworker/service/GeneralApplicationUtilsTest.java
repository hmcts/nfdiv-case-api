package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.findGeneralApplicationIndexByLabel;
import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.formatter;
import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.populateGeneralApplicationList;
import static uk.gov.hmcts.divorce.caseworker.service.GeneralApplicationUtils.populateUnpaidGeneralApplicationList;
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

        assertThat(caseData.getGeneralReferral().getSelectedGeneralApplication().getListItems()).hasSize(4);
    }

    @Test
    void shouldReturnIndexOfActiveGeneralApplicationForGivenApplicant() {
        Applicant applicant = Applicant.builder()
            .generalAppServiceRequest(TEST_SERVICE_REFERENCE)
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .generalApplications(buildListOfGeneralApplications())
            .build();

        OptionalInt activeGeneralApplicationIndex = GeneralApplicationUtils.findActiveGeneralApplicationIndex(caseData, applicant);

        assertThat(activeGeneralApplicationIndex.isPresent()).isTrue();
        assertThat(activeGeneralApplicationIndex.getAsInt()).isEqualTo(0);
    }

    @Test
    void shouldReturnEmptyWhenGeneralApplicationIsNotFound() {
        Applicant applicant = Applicant.builder()
            .generalAppServiceRequest("invalidServiceRequest")
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .generalApplications(buildListOfGeneralApplications())
            .build();

        OptionalInt activeGeneralApplicationIndex = GeneralApplicationUtils.findActiveGeneralApplicationIndex(caseData, applicant);

        assertThat(activeGeneralApplicationIndex.isPresent()).isFalse();
    }

    @Test
    void shouldReturnEmptyWhenGeneralApplicationCollectionIsNull() {
        Applicant applicant = Applicant.builder()
            .generalAppServiceRequest("invalidServiceRequest")
            .build();

        CaseData caseData = CaseData.builder()
            .applicant1(applicant)
            .generalApplications(null)
            .build();

        OptionalInt activeGeneralApplicationIndex = GeneralApplicationUtils.findActiveGeneralApplicationIndex(caseData, applicant);

        assertThat(activeGeneralApplicationIndex.isPresent()).isFalse();
    }

    @Test
    void shouldPopulateUnpaidGeneralApplicationList() {
        final CaseData caseData = caseDataWithMarriageDate();
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        caseData.setGeneralApplications(generalApplications);

        populateUnpaidGeneralApplicationList(caseData);

        assertThat(caseData.getGeneralReferral().getSelectedGeneralApplication().getListItems()).hasSize(1);
    }

    @Test
    void shouldReturnGeneralApplicationIndexForProvidedLabel() {
        CaseData caseData = CaseData.builder()
            .generalApplications(buildListOfGeneralApplications())
            .build();

        String labelToSearch = caseData.getGeneralApplications().get(0).getValue().getLabel(0, formatter);

        OptionalInt activeGeneralApplicationIndex = findGeneralApplicationIndexByLabel(caseData, labelToSearch);

        assertThat(activeGeneralApplicationIndex.isPresent()).isTrue();
        assertThat(activeGeneralApplicationIndex.getAsInt()).isEqualTo(0);
    }

    @Test
    void shouldReturnBlankForBlankLabel() {
        CaseData caseData = CaseData.builder()
            .generalApplications(buildListOfGeneralApplications())
            .build();

        OptionalInt activeGeneralApplicationIndex = findGeneralApplicationIndexByLabel(caseData, null);

        assertThat(activeGeneralApplicationIndex.isPresent()).isFalse();
    }

    @Test
    void shouldReturnBlankWhenNoGeneralApplicationsPresent() {
        CaseData caseData = CaseData.builder()
            .generalApplications(null)
            .build();

        String labelToSearch = "testLabel";

        OptionalInt activeGeneralApplicationIndex = findGeneralApplicationIndexByLabel(caseData, labelToSearch);

        assertThat(activeGeneralApplicationIndex.isPresent()).isFalse();
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
                            .hasCompletedOnlinePayment(YesOrNo.YES)
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
                            .serviceRequestReference("dummyServiceRequest")
                            .hasCompletedOnlinePayment(YesOrNo.YES)
                            .build()
                    )
                    .generalApplicationReceivedDate(LocalDateTime.of(2022, 1, 1, 1, 1, 1))
                    .build()
            ).build(),
            ListValue.<GeneralApplication>builder().value(
                GeneralApplication.builder()
                    .generalApplicationType(GeneralApplicationType.WITHDRAW_POST_ISSUE)
                    .generalApplicationSubmittedOnline(YesOrNo.YES)
                    .generalApplicationParty(GeneralParties.APPLICANT)
                    .generalApplicationFee(
                        FeeDetails.builder()
                            .serviceRequestReference("dummyServiceRequest")
                            .hasCompletedOnlinePayment(YesOrNo.NO)
                            .build()
                    )
                    .generalApplicationReceivedDate(LocalDateTime.of(2022, 1, 1, 1, 1, 1))
                    .build()
            ).build(),
            ListValue.<GeneralApplication>builder().value(
                GeneralApplication.builder()
                    .generalApplicationType(GeneralApplicationType.AMEND_APPLICATION)
                    .generalApplicationSubmittedOnline(YesOrNo.NO)
                    .generalApplicationParty(GeneralParties.APPLICANT)
                    .generalApplicationFee(
                        FeeDetails.builder()
                            .serviceRequestReference("dummyServiceRequest")
                            .build()
                    )
                    .generalApplicationReceivedDate(LocalDateTime.of(2022, 1, 1, 1, 1, 1))
                    .build()
            ).build()
        );
    }
}
