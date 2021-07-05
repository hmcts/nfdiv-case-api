package uk.gov.hmcts.divorce.bulkaction.ccd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.common.model.access.CaseAccessAdministrator;

@RequiredArgsConstructor
@Getter
public enum BulkActionState {

    @CCD(
        name = "Draft",
        label = "# **${[CASE_REFERENCE]}** ${applicant1LastName} **&** ${applicant2LastName}\n### **${[STATE]}**\n",
        access = {CaseAccessAdministrator.class}
    )
    Draft("Draft");

    private final String name;
}

