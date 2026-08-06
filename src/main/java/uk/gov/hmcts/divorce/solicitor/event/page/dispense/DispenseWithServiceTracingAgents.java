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

public class DispenseWithServiceTracingAgents implements CcdPageConfiguration {

    private static final String LABEL_TRACING_AGENTS = """
        ### Tracing agents ###

        You could consider employing a tracing agent to try to find the respondent's whereabouts or their contact details.

        If tracing agent can find up to date details, you can use those to progress the ${labelContentUnionType} application another way.
        You do not need to continue this application to dispense with service.

        If the tracing agent fails to find the respondent, they should contact you with the results of any searches they do, which
        you can use as evidence.
        """;

    private static final String TRIED_TRACING_AGENT_SHOW_CONDITION_YES = "applicant1DispenseTriedTracingAgent=\"Yes\"";
    private static final String TRIED_TRACING_AGENT_SHOW_CONDITION_NO = "applicant1DispenseTriedTracingAgent=\"No\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceTracingAgents")
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
                        .label("labelRespondentTracingAgents", LABEL_TRACING_AGENTS)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseTriedTracingAgent)
                        .label("labelTracingAgentSearchResults", "### Tracing agents search ###",
                            TRIED_TRACING_AGENT_SHOW_CONDITION_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseTracingAgentResults,
                            TRIED_TRACING_AGENT_SHOW_CONDITION_YES)
                        .label("labelYouCanUploadTracingAgentEvidence", LABEL_CAN_UPLOAD_EVIDENCE_DISPENSE_SERVICE,
                            TRIED_TRACING_AGENT_SHOW_CONDITION_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseWhyNoTracingAgent,
                            TRIED_TRACING_AGENT_SHOW_CONDITION_NO)
                    .done()
                .done()
            .done();
    }
}
