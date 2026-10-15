package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;
import java.util.Collections;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DispenseWithServiceLivedTogetherAnyTime implements CcdPageConfiguration {

    private static final String LIVED_TOGETHER_ANY_TIME_SHOW_CONDITION = "applicant1DispenseLiveTogether=\"Yes\"";
    private static final String FUTURE_DATE_ERROR = "You cannot enter a date in the future";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceLivedTogether", this::midEvent);

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page.complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDispenseWithServiceJourneyOptions)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseLiveTogether)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseLivedTogetherDate,
                                LIVED_TOGETHER_ANY_TIME_SHOW_CONDITION)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseLivedTogetherAddress,
                                LIVED_TOGETHER_ANY_TIME_SHOW_CONDITION)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseLivedTogetherAddressOverseas,
                                LIVED_TOGETHER_ANY_TIME_SHOW_CONDITION)
                    .done()
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        final CaseData data = details.getData();
        final DispenseWithServiceJourneyOptions dispenseWithServiceJourneyOptions =
            data.getApplicant1().getInterimApplicationOptions().getDispenseWithServiceJourneyOptions();

        LocalDate lastSeenDate = dispenseWithServiceJourneyOptions.getDispenseLivedTogetherDate();

        boolean isFutureDate = lastSeenDate != null && lastSeenDate.isAfter(LocalDate.now());
        if (isFutureDate) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .errors(Collections.singletonList(FUTURE_DATE_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
