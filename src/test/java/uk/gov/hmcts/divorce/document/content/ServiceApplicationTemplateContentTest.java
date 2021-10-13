package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_DECISION_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_RECEIVED_DATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ServiceApplicationTemplateContentTest {

    @Autowired
    private ServiceApplicationTemplateContent serviceApplicationTemplateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingDispensedWithServiceGrantedDocument() {
        CaseData caseData = caseData();
        caseData.setAlternativeService(
            AlternativeService
                .builder()
                .serviceApplicationGranted(YES)
                .alternativeServiceType(DISPENSED)
                .serviceApplicationDecisionDate(LocalDate.of(2021, 6, 18))
                .receivedServiceApplicationDate(LocalDate.of(2021, 6, 18))
                .build()
        );
        caseData.getApplication().getMarriageDetails().setApplicant1Name("pet full name");
        caseData.getApplication().getMarriageDetails().setApplicant2Name("resp full name");

        Map<String, Object> templateContent = serviceApplicationTemplateContent.apply(caseData, TEST_CASE_ID);

        var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName("HMCTS Digital Divorce")
            .emailAddress("divorcecase@justice.gov.uk")
            .serviceCentre("Courts and Tribunals Service Centre")
            .poBox("PO Box 12706")
            .town("Harlow")
            .postcode("CM20 9QT")
            .phoneNumber("0300 303 0642")
            .build();

        assertThat(templateContent).contains(
            entry(CASE_REFERENCE, 1616591401473378L),
            entry(DOCUMENTS_ISSUED_ON, "18 June 2021"),
            entry(SERVICE_APPLICATION_RECEIVED_DATE, "18 June 2021"),
            entry(SERVICE_APPLICATION_DECISION_DATE, "18 June 2021"),
            entry(PETITIONER_FULL_NAME, "pet full name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry("ctscContactDetails", ctscContactDetails)
        );
    }
}
