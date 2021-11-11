package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.common.ConditionalOrderCourtDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COSTS_GRANTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties
@AutoConfigureMockMvc
class CertificateOfEntitlementContentIT {

    @Autowired
    private CertificateOfEntitlementContent certificateOfEntitlementContent;

    @Test
    void shouldReturnTemplateSetFromCaseDataAndCourtDetailsConfig() {

        final LocalDateTime localDateTime = LocalDateTime.of(2021, 11, 8, 14, 56);
        final LocalDate localDate = LocalDate.of(2021, 11, 8);
        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .applicant1(Applicant.builder()
                .firstName("John")
                .lastName("Smith")
                .financialOrder(YES)
                .build())
            .applicant2(Applicant.builder()
                .firstName("Jane")
                .lastName("Jones")
                .build())
            .conditionalOrder(ConditionalOrder.builder()
                .court(BURY_ST_EDMUNDS)
                .dateAndTimeOfHearing(localDateTime)
                .decisionDate(localDate)
                .claimsGranted(YES)
                .claimsCostsOrderInformation("info")
                .build())
            .application(Application.builder()
                .divorceWho(HUSBAND)
                .build())
            .build();

        final ConditionalOrderCourtDetails expectedDetails = new ConditionalOrderCourtDetails();
        expectedDetails.setName("Bury St. Edmunds Regional Divorce Centre");
        expectedDetails.setAddress("2nd Floor\nTriton House\nSt. Andrews Street North\nBury St. Edmunds\nIP33 1TR");
        expectedDetails.setEmail("divorcecase@justice.gov.uk");
        expectedDetails.setPhone("0300 303 0642");

        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        assertThat(contentMap).contains(
            entry(CCD_CASE_REFERENCE, "1616-5914-0147-3378"),
            entry("courtDetails", expectedDetails),
            entry("approvalDate", "8 November 2021"),
            entry(APPLICANT_1_FIRST_NAME, "John"),
            entry(APPLICANT_1_LAST_NAME, "Smith"),
            entry(APPLICANT_2_FIRST_NAME, "Jane"),
            entry(APPLICANT_2_LAST_NAME, "Jones"),
            entry("isSole", true),
            entry("isJoint", false),
            entry(DATE_OF_HEARING, "8 November 2021"),
            entry(TIME_OF_HEARING, "14:56 pm"),
            entry(HAS_FINANCIAL_ORDERS, true),
            entry(COSTS_GRANTED, true),
            entry("claimsCostsOrderInformation", "info"),
            entry("divorceWho", "husband")
        );
    }
}