package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetApplicantOfflineStatusTest {

    @InjectMocks
    SetApplicantOfflineStatus task;

    @Test
    void testSetsApplicant1AsOnline() {
        final var details = new CaseDetails<CaseData, State>();
        details.setData(caseData());

        final var result = task.apply(details);

        assertEquals(result.getData().getApplicant1().getOffline(), NO);
    }

    @Test
    void testSetsApplicant2AsOnlineIfRepresentedAndOrganisationPolicyIsPresent() {
        final var details = new CaseDetails<CaseData, State>();
        final var data = caseData();

        data.getApplicant2().setSolicitorRepresented(YES);
        data.getApplicant2().setSolicitor(
            Solicitor
                .builder()
                .organisationPolicy(
                    OrganisationPolicy
                        .<UserRole>builder()
                        .organisation(
                            Organisation
                                .builder()
                                .organisationId("ORG123")
                                .build()
                        )
                        .build()
                )
                .address("some address")
                .name("some sol")
                .build()
        );
        details.setData(data);

        final var result = task.apply(details);

        assertEquals(result.getData().getApplicant2().getOffline(), NO);
    }

    @Test
    void testSetsApplicant2AsOnlineIfNotRepresentedButEmailProvided() {
        final var details = new CaseDetails<CaseData, State>();
        final var data = caseData();

        data.getApplicant2().setSolicitorRepresented(NO);
        data.getApplicant2().setEmail("not empty");
        details.setData(data);

        final var result = task.apply(details);

        assertEquals(result.getData().getApplicant2().getOffline(), NO);
    }

    @Test
    void testSetsApplicant2AsOfflineIfNotRepresentedAndNoEmail() {
        final var details = new CaseDetails<CaseData, State>();
        final var data = caseData();

        data.getApplicant2().setSolicitorRepresented(NO);
        data.getApplicant2().setEmail("");
        details.setData(data);

        final var result = task.apply(details);

        assertEquals(result.getData().getApplicant2().getOffline(), YES);
    }

    @Test
    void testSetsApplicant2AsOfflineIfRepresentedAndDoesNotHaveOrganisationPolicy() {
        final var details = new CaseDetails<CaseData, State>();
        final var data = caseData();
        data.getApplicant2().setSolicitor(
            Solicitor
                .builder()
                .address("some address")
                .name("some sol")
                .build()
        );
        data.getApplicant2().setSolicitorRepresented(YES);
        details.setData(data);

        final var result = task.apply(details);

        assertEquals(result.getData().getApplicant2().getOffline(), YES);
    }
}
