package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class GeneralApplicationD11Page5 implements CcdPageConfiguration {

    private final TypedPropertyGetter<CaseData, Applicant> applicantRef;

    private final String evidenceOfConsentRequired;

    private static final String UPLOAD_LABEL = "Upload evidence";

    public static final String CONSENT_UPLOAD_LABEL = """
        ## Upload your evidence of consent from the other party

        Provide evidence that the other party agrees (consents) to this application.

        Suitable evidence may include:
        - A signed statement from both parties agreeing to this application
        - A letter or email from the other party saying they agree to this application

        If you are uploading images or screenshots of a recent conversation, it may help if they show:
        - The other party's name
        - The date the letter or email was sent
        - Their contact details
        """;

    public GeneralApplicationD11Page5(TypedPropertyGetter<CaseData, Applicant> applicantRef, String applicantFieldPrefix) {
        this.applicantRef = applicantRef;
        this.evidenceOfConsentRequired = applicantFieldPrefix + "GenAppHearingNotRequired=\"yesPartnerAgreesWithApplication\" "
            + "OR " + applicantFieldPrefix + "GenAppHearingNotRequired=\"yesPartnerAgreesWithNoHearing\"";
    }

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("SolGenAppD11ConsentEvidence");
        String combinedCondition = evidenceOfConsentRequired;
        if (isNotBlank(pageShowCondition)) {
            combinedCondition = pageShowCondition + " AND (" + evidenceOfConsentRequired + ")";
        }
        page.showCondition(combinedCondition)
            .label("solGenAppConsentEvidenceLabel", CONSENT_UPLOAD_LABEL)
            .complex(applicantRef)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getGeneralApplicationD11JourneyOptions)
                        .mandatory(GeneralApplicationD11JourneyOptions::getPartnerAgreesDocs, null, null, UPLOAD_LABEL)
                    .done()
                .done()
            .done();
    }
}
