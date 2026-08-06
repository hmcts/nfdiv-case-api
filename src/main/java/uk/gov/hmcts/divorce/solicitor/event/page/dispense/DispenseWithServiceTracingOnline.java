package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceRespondentEmailAddress.LABEL_CAN_UPLOAD_EVIDENCE_DISPENSE_SERVICE;

public class DispenseWithServiceTracingOnline implements CcdPageConfiguration {

    private static final String LABEL_TRACING_RESPONDENT_ONLINE = """
        ### Tracing the respondent online ###

        You could consider using online people finding services to try to find the respondent's details.

        If you can find the respondent's address or contact details, you could try progressing the ${labelContentUnionType} application
        another way.
        """;

    private static final String TRIED_TRACING_ONLINE_SHOW_CONDITION_YES = "applicant1DispenseTriedTracingOnline=\"Yes\"";
    private static final String TRIED_TRACING_ONLINE_SHOW_CONDITION_NO = "applicant1DispenseTriedTracingOnline=\"No\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceOnlineTracing")
                    .pageLabel("Dispense with service app");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page
            .complex(CaseData::getLabelContent)
            .readonlyNoSummary(LabelContent::getUnionType, NEVER_SHOW)
            .done()
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDispenseWithServiceJourneyOptions)
                        .label("labelRespondentTracingOnline", LABEL_TRACING_RESPONDENT_ONLINE)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseTriedTracingOnline)
                        .label("labelTracingOnlineResults", "### Online tracing results ###",
                            TRIED_TRACING_ONLINE_SHOW_CONDITION_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseTracingOnlineResults,
                            TRIED_TRACING_ONLINE_SHOW_CONDITION_YES)
                        .label("labelYouCanUploadTracingOnlineEvidence", LABEL_CAN_UPLOAD_EVIDENCE_DISPENSE_SERVICE,
                            TRIED_TRACING_ONLINE_SHOW_CONDITION_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseWhyNoTracingOnline,
                            TRIED_TRACING_ONLINE_SHOW_CONDITION_NO)
                    .done()
                .done()
            .done();
    }
}
