package uk.gov.hmcts.reform.divorce.ccd.event;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdBuilder;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.reform.divorce.ccd.model.CaseEvent.DRAFT_CREATE;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.DRAFT;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CITIZEN;

public class DraftCreate implements CcdBuilder {

    @Override
    public void buildWith(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(DRAFT_CREATE.name)
            .initialState(DRAFT)
            .name("Create draft case")
            .description("Apply for a divorce or dissolution")
            .displayOrder(1)
            .retries(120, 120)
            .grant("CRU", CITIZEN)
            .grant("R",
                CASEWORKER_DIVORCE_COURTADMIN_BETA,
                CASEWORKER_DIVORCE_COURTADMIN,
                CASEWORKER_DIVORCE_SOLICITOR,
                CASEWORKER_DIVORCE_SUPERUSER,
                CASEWORKER_DIVORCE_COURTADMIN_LA);

        configBuilder
            .workBasketInputFields()
            .caseReferenceField();

        configBuilder
            .workBasketResultFields()
            .caseReferenceField();

        //configBuilder
        //    .searchInputFields()
        //    .caseReferenceField();

        //configBuilder
        //    .searchResultFields()
        //    .caseReferenceField();
    }
}
