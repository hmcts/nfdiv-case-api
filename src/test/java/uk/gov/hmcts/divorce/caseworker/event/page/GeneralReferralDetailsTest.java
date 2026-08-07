package uk.gov.hmcts.divorce.caseworker.event.page;

import org.apache.commons.collections.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceMediumType.TEXT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason.GENERAL_APPLICATION_REFERRAL;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType.CASEWORKER_REFERRAL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getListOfDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
class GeneralReferralDetailsTest {

    @InjectMocks
    private GeneralReferralDetails generalReferralDetails;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm:ss a");

    @Test
    void shouldUpdateGeneralReferralWithGeneralApplicationDocuments() {
        List<ListValue<GeneralApplication>> generalApplications = buildListOfGeneralApplications();
        final CaseDetails<CaseData, State> details = buildTestCaseDetails(generalApplications);

        final AboutToStartOrSubmitResponse<CaseData, State> response = generalReferralDetails.midEvent(details, details);

        assertThat(response.getData().getGeneralReferral().getGeneralReferralDocuments().size()).isEqualTo(2);
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
                    .generalApplicationDocuments(getListOfDivorceDocumentListValue(2))
                    .build()
            ).build()
        );
    }

    private CaseDetails<CaseData, State> buildTestCaseDetails(List<ListValue<GeneralApplication>> generalApplications) {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setGeneralAppServiceRequest(TEST_SERVICE_REFERENCE);
        caseData.getApplicant1().setGeneralAppPayments(List.of(
            ListValue.<Payment>builder().value(
                Payment.builder().amount(10).build()
            ).build()
        ));
        caseData.setGeneralApplications(generalApplications);
        caseData.setGeneralReferral(generalReferral(NO));
        caseData.getGeneralReferral().setGeneralReferralReason(GeneralReferralReason.GENERAL_APPLICATION_REFERRAL);
        if (CollectionUtils.isNotEmpty(generalApplications)) {
            caseData.getGeneralReferral().setSelectedGeneralApplication(DynamicList
                .builder()
                .value(DynamicListElement.builder().label(generalApplications.getLast().getValue().getLabel(1, formatter)).build()
                ).build()
            );
        }
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        return details;
    }

    private GeneralReferral generalReferral(YesOrNo feeRequired) {
        return GeneralReferral
            .builder()
            .generalApplicationReferralDate(LocalDate.now())
            .generalApplicationFrom(APPLICANT)
            .generalReferralFeeRequired(feeRequired)
            .generalReferralType(CASEWORKER_REFERRAL)
            .generalReferralReason(GENERAL_APPLICATION_REFERRAL)
            .alternativeServiceMedium(TEXT)
            .generalReferralJudgeOrLegalAdvisorDetails("some judge legal advisor details")
            .build();
    }
}
