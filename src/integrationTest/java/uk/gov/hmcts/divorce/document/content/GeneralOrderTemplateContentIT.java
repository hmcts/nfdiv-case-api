package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.GENERAL_ORDER_RECITALS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDGE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDGE_TYPE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getGeneralOrder;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class GeneralOrderTemplateContentIT {

    @Autowired
    private GeneralOrderTemplateContent templateContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingGeneralOrderDocument() {
        CaseData caseData = caseData();
        caseData.setGeneralOrder(getGeneralOrder());
        caseData.getApplication().getMarriageDetails().setApplicant1Name("pet full name");
        caseData.getApplication().getMarriageDetails().setApplicant2Name("resp full name");

        Supplier<Map<String, Object>> templateContentSupplier = templateContent.apply(caseData, TEST_CASE_ID);

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

        assertThat(templateContentSupplier.get()).contains(
            entry(CASE_REFERENCE, 1616591401473378L),
            entry(GENERAL_ORDER_DATE, "1 January 2021"),
            entry(GENERAL_ORDER_DETAILS, "some details"),
            entry(GENERAL_ORDER_RECITALS, "test recitals"),
            entry(JUDGE_NAME, "some name"),
            entry(JUDGE_TYPE, "Recorder"),
            entry(PETITIONER_FULL_NAME, "pet full name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry("ctscContactDetails", ctscContactDetails)
        );
    }
}
