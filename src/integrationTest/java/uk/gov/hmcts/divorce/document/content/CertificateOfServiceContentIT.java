package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CertificateOfServiceContentIT {

    @Autowired
    private CertificateOfServiceContent certificateOfServiceContent;

    @Test
    public void shouldSuccessfullyApplyContentFromCaseDataForGeneratingCertificateOfServiceDocument() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setFirstName("pet full");
        caseData.getApplicant1().setLastName("name");
        caseData.getApplicant2().setFirstName("resp full");
        caseData.getApplicant2().setLastName("name");
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);

        Map<String, Object> templateContent = certificateOfServiceContent.apply(caseData, TEST_CASE_ID);

        assertThat(templateContent).contains(
            entry(CCD_CASE_REFERENCE, 1616591401473378L),
            entry(PETITIONER_FULL_NAME, "pet full test_middle_name name"),
            entry(RESPONDENT_FULL_NAME, "resp full name"),
            entry(IS_DIVORCE, "Yes"),
            entry(DOCUMENTS_ISSUED_ON, LocalDate.now().format(DATE_TIME_FORMATTER))
        );
    }
}
