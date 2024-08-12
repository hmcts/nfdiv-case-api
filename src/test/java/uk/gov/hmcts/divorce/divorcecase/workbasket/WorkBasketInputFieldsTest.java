package uk.gov.hmcts.divorce.divorcecase.workbasket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getWorkBasketInputFields;

class WorkBasketInputFieldsTest {
    private WorkBasketInputFields workBasketInputFields;

    @BeforeEach
    void setUp() {
        workBasketInputFields = new WorkBasketInputFields();
    }

    @Test
    void shouldSetWorkBasketResultFields() throws Exception {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        workBasketInputFields.configure(configBuilder);

        assertThat(getWorkBasketInputFields(configBuilder).getFields())
            .extracting("id",
                "label",
                "listElementCode",
                "showCondition")
            .contains(
                tuple("[CASE_REFERENCE]",
                    "CCD reference",
                    null,
                    null),
                tuple("marriageDate",
                    "Marriage date",
                    null,
                    null),
                tuple("applicant1HWFReferenceNumber",
                    "Applicant 1 HWF reference",
                    null,
                    null),
                tuple("applicant2HWFReferenceNumber",
                    "Applicant 2 HWF reference",
                    null,
                    null),
                tuple("solUrgentCase",
                    "Urgent case",
                    null,
                    null),
                tuple("generalReferralUrgentCase",
                    "Urgent general referral case",
                    null,
                    null),
                tuple("applicant1SolicitorFirmName",
                    "Solicitor firm name",
                    null,
                    null),
                tuple("alternativeServiceType",
                    "Type of service",
                    null,
                    null),
                tuple("applicant1Address",
                    "Applicant postcode",
                    "PostCode",
                    null),
                tuple("applicant2Address",
                    "Respondent postcode",
                    "PostCode",
                    null),
                tuple("applicant1Email",
                    "Applicant email",
                    null,
                    null),
                tuple("applicant2Email",
                    "Respondent email",
                    null,
                    null),
                tuple("applicant1FirstName",
                    "Applicant first name",
                    null,
                    null),
                tuple("applicant1LastName",
                    "Applicant last name",
                    null,
                    null),
                tuple("applicant2FirstName",
                    "Respondent first name",
                    null,
                    null),
                tuple("applicant2LastName",
                    "Respondent last name",
                    null,
                    null),
                tuple("evidenceHandled",
                    "Supplementary evidence handled",
                    null,
                    null)
            );
    }
}
