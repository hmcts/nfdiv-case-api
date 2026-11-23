package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class GeneralApplicationD11Page8 implements CcdPageConfiguration {

    private final TypedPropertyGetter<CaseData, Applicant> applicant1Ref;

    private final TypedPropertyGetter<CaseData, Applicant> applicant2Ref;

    public GeneralApplicationD11Page8(TypedPropertyGetter<CaseData, Applicant> applicant1Ref,
                                      TypedPropertyGetter<CaseData, Applicant> applicant2Ref) {
        this.applicant1Ref = applicant1Ref;
        this.applicant2Ref = applicant2Ref;
    }

    public static final String PARTY_DETAILS_LABEL = """
        ## We need up to date information for the other party
        We need up to date contact details so that we can send the application to the other party.
        """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("SolGenAppD11PartyDetails");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page.label("solGeneralAppD11PartyDetailsLabel", PARTY_DETAILS_LABEL)
            .complex(applicant2Ref)
                .readonlyNoSummary(Applicant::getAddress)
                .readonlyNoSummary(Applicant::getEmail)
            .done()
            .complex(applicant1Ref)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getGeneralApplicationD11JourneyOptions)
                        .mandatory(GeneralApplicationD11JourneyOptions::getPartnerDetailsCorrect)
                    .done()
                .done()
            .done();
    }
}
