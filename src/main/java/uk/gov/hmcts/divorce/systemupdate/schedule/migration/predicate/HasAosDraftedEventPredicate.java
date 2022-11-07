package uk.gov.hmcts.divorce.systemupdate.schedule.migration.predicate;

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

    /**
     * isAosDrafted is set to Yes when the DRAFT_AOS event occurs, but it is reset on CASEWORKER_REISSUE_APPLICATION event.
     *
     * <p>This predicate checks for the DRAFT_AOS event in the Case event history, but It will only return true if a
     * CASEWORKER_REISSUE_APPLICATION event does not occur after the DRAFT_AOS event.
     *
     * @param user                 - for authentication to CCD
     * @param serviceAuthorization - for authorization to CCD
     * @return true - if DRAFT_AOS event is present and CASEWORKER_REISSUE_APPLICATION event does not occur after DRAFT_AOS event.
     */
    public Predicate<CaseDetails> hasAosDraftedEvent(final User user, final String serviceAuthorization) {

        return caseDetails -> {
            final Long caseId = caseDetails.getId();

            try {
                //Get list of events ordered by created date
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

                    //Push DRAFT_AOS and CASEWORKER_REISSUE_APPLICATION onto stack to check which comes last.
                    //First event on the stack will be the last event in history
                    if (eventId.equals(DRAFT_AOS) || eventId.equals(CASEWORKER_REISSUE_APPLICATION)) {
                        stack.push(caseEventDetail);
                    }
                }

                if (!stack.isEmpty()) {
                    //Only return true if DRAFT_AOS is the first event on the stack
                    return stack.pop().getId().equals(DRAFT_AOS);
                }
            } catch (final FeignException e) {
                log.error("Failed to retrieve event history, skipping Case Id {}", caseId);
            }

            //Return false if there is no DRAFT_AOS event or CASEWORKER_REISSUE_APPLICATION occurs after DRAFT_AOS event
            return false;
        };
    }
}
