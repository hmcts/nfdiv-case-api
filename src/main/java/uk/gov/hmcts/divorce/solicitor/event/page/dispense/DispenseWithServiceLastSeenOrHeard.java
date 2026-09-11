package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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

@Slf4j
public class DispenseWithServiceLastSeenOrHeard implements CcdPageConfiguration {

    private static final String LABEL_LAST_SEEN_DESCRIPTION = """
        Describe the last time that the applicant saw or heard from the respondent. Include the source of this information and
        give brief details of all the enquiries made to trace them as a result.
        """;

    private static final String FUTURE_DATE_ERROR = "You cannot enter a date in the future";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceLastSeen", this::midEvent);

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDispenseWithServiceJourneyOptions)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispensePartnerLastSeenDate)
                        .label("labelLastSeenDescription", LABEL_LAST_SEEN_DESCRIPTION)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispensePartnerLastSeenDescription)
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

        LocalDate lastSeenDate = dispenseWithServiceJourneyOptions.getDispensePartnerLastSeenDate();

        boolean isFutureDate = lastSeenDate != null && lastSeenDate.isAfter(LocalDate.now());
        if (isFutureDate) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(data)
                    .errors(Collections.singletonList(FUTURE_DATE_ERROR))
                    .build();
        }

        boolean beenMoreThan2Years = lastSeenDate != null && lastSeenDate.isBefore(LocalDate.now().minusYears(2));
        dispenseWithServiceJourneyOptions
                .setDispensePartnerLastSeenOver2YearsAgo(beenMoreThan2Years ? YesOrNo.YES : YesOrNo.NO);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .build();
    }
}
