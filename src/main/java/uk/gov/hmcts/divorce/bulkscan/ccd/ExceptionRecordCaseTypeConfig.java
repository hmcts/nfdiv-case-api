package uk.gov.hmcts.divorce.bulkscan.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.type.ExceptionRecord;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

@Component
public class ExceptionRecordCaseTypeConfig implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    public static final String CASE_TYPE = "NFD_ExceptionRecord";
    public static final String JURISDICTION = "DIVORCE";

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        configBuilder.caseType(CASE_TYPE, "New law case exception record", "Exception record for new law case");
        configBuilder.jurisdiction(JURISDICTION, "Family Divorce", "Manage new law case exception records");
    }
}
