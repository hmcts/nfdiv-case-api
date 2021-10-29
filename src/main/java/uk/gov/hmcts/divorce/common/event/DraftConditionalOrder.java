package uk.gov.hmcts.divorce.common.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.ConditionalOrderNewDocuments;
import uk.gov.hmcts.divorce.common.event.page.ConditionalOrderReviewAoS;
import uk.gov.hmcts.divorce.common.event.page.ConditionalOrderReviewApplicant1;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class DraftConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String DRAFT_CONDITIONAL_ORDER = "draft-conditional-order";

    private final List<CcdPageConfiguration> pages = asList(
        new ConditionalOrderReviewAoS(),
        new ConditionalOrderReviewApplicant1(),
        new ConditionalOrderNewDocuments()
    );

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(DRAFT_CONDITIONAL_ORDER)
            .forStateTransition(AwaitingConditionalOrder, ConditionalOrderDrafted)
            .name("Draft Conditional Order")
            .description("Draft Conditional Order")
            .showSummary()
            .endButtonLabel("Save Conditional Order")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR, CREATOR)
            .grant(READ,
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR));
    }

}
