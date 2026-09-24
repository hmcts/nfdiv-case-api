package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class GeneralApplicationD11Page2 implements CcdPageConfiguration {

    private final TypedPropertyGetter<CaseData, Applicant> applicantRef;

    private final String otherApplicationCondition;

    private final String urgentCaseCondition;

    public GeneralApplicationD11Page2(TypedPropertyGetter<CaseData, Applicant> applicantRef, String applicantFieldPrefix) {
        this.applicantRef = applicantRef;
        this.otherApplicationCondition = applicantFieldPrefix + "GenAppSolType=\"other\"";
        this.urgentCaseCondition = applicantFieldPrefix + "GenAppUrgentCase=\"Yes\"";
    }

    public static final String OTHER_MORE_INFO_LABEL = """
        Please provide more information about the general application
        """;

    public static final String STATEMENT_LABEL = """
       Do you want to provide a statement or upload evidence?
        """;

    public static final String STATEMENT_HINT = """
        You should provide as much detail as you can. Your statement and any evidence you provide will help the court decide whether to grant your application.
        """;

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("SolGenAppD11Details")
            .complex(applicantRef)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getGeneralApplicationD11JourneyOptions)
                        .mandatory(GeneralApplicationD11JourneyOptions::getSolType)
                        .mandatory(GeneralApplicationD11JourneyOptions::getTypeOtherDetails, otherApplicationCondition, null,
                    OTHER_MORE_INFO_LABEL)
                        .mandatory(GeneralApplicationD11JourneyOptions::getUrgentCase)
                        .mandatory(GeneralApplicationD11JourneyOptions::getUrgentCaseReason, urgentCaseCondition)
                        .mandatory(GeneralApplicationD11JourneyOptions::getReason)
                    .done()
                    .mandatory(InterimApplicationOptions::getInterimAppsCanUploadEvidence, ALWAYS_SHOW, null,
                        STATEMENT_LABEL, STATEMENT_HINT)
                .done()
            .done();
    }
}
