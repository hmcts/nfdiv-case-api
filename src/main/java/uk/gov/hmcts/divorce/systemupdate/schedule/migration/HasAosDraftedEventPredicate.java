package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.CaseEventsApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Comparator.comparing;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerReissueApplication.CASEWORKER_REISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.common.event.DraftAos.DRAFT_AOS;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;

@Component
@Slf4j
public class HasAosDraftedEventPredicate {

    @Autowired
    private CaseEventsApi caseEventsApi;

    public Predicate<CaseDetails> hasAosDraftedEvent(final User user, final String serviceAuthorization) {

        return caseDetails -> {
            final Long caseId = caseDetails.getId();

            try {
                final List<CaseEventDetail> eventDetailsForCase = caseEventsApi.findEventDetailsForCase(
                        user.getAuthToken(),
                        serviceAuthorization,
                        user.getUserDetails().getId(),
                        JURISDICTION,
                        CASE_TYPE,
                        caseId.toString())
                    .stream()
                    .sorted(comparing(CaseEventDetail::getCreatedDate))
                    .toList();

                final Deque<CaseEventDetail> stack = new LinkedList<>();
                for (CaseEventDetail caseEventDetail : eventDetailsForCase) {
                    final String eventId = caseEventDetail.getId();
                    if (eventId.equals(DRAFT_AOS) || eventId.equals(CASEWORKER_REISSUE_APPLICATION)) {
                        stack.push(caseEventDetail);
                    }
                }

                if (!stack.isEmpty()) {
                    return stack.pop().getId().equals(DRAFT_AOS);
                }
            } catch (final FeignException e) {
                log.error("Failed to retrieve event history, skipping Case Id {}", caseId);
            }

            return false;
        };
    }
}
