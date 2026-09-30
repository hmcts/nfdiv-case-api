package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class GeneralApplicationD11Page4 implements CcdPageConfiguration {

    private final TypedPropertyGetter<CaseData, Applicant> applicantRef;

    public GeneralApplicationD11Page4(TypedPropertyGetter<CaseData, Applicant> applicantRef) {
        this.applicantRef = applicantRef;
    }

    public static final String HEARING_SECTION_LABEL = """
        ## Dealing with your application without a hearing
        Generally, if the other party agrees (consents) with your application, the court may be able to
        deal with the application 'on paper' (without a hearing).
        This will usually mean that your application is faster and less expensive. You will need to provide
        written evidence of consent from the other party.
        """;

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("SolGenAppD11Hearing")
            .label("solGeneralApplicationHearingLabel", HEARING_SECTION_LABEL)
            .complex(applicantRef)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getGeneralApplicationD11JourneyOptions)
                        .mandatory(GeneralApplicationD11JourneyOptions::getHearingNotRequired)
                    .done()
                .done()
            .done();
    }
}
