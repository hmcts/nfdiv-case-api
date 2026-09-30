package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class GeneralApplicationD11Page3 implements CcdPageConfiguration {

    private final TypedPropertyGetter<CaseData, Applicant> applicantRef;

    private final String canUploadEvidenceCondition;

    public GeneralApplicationD11Page3(TypedPropertyGetter<CaseData, Applicant> applicantRef, String applicantFieldPrefix) {
        this.applicantRef = applicantRef;
        this.canUploadEvidenceCondition = applicantFieldPrefix + "InterimAppsCanUploadEvidence=\"Yes\"";
    }

    public static final String EVIDENCE_SECTION_LABEL = """
        ## Provide a statement or upload evidence
        You can provide a statement and upload any documents you have in support of your application.
        """;

    public static final String UPLOAD_LABEL = "Upload evidence";

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("SolGenAppD11Evidence")
            .label("solGeneralApplicationEvidenceLabel", EVIDENCE_SECTION_LABEL)
            .showCondition(canUploadEvidenceCondition)
            .complex(applicantRef)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getGeneralApplicationD11JourneyOptions)
                        .optional(GeneralApplicationD11JourneyOptions::getStatementOfEvidence)
                    .done()
                    .optionalWithLabel(InterimApplicationOptions::getInterimAppsEvidenceDocs, UPLOAD_LABEL)
                .done()
            .done();
    }
}
