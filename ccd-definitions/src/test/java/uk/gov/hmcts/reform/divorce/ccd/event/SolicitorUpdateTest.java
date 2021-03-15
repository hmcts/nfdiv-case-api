package uk.gov.hmcts.reform.divorce.ccd.event;

import de.cronn.reflection.util.TypedPropertyGetter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.EventTypeBuilder;
import uk.gov.hmcts.ccd.sdk.api.Field;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.reform.divorce.ccd.mock.EventBuildingMockUtil;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Mandatory;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Optional;
import static uk.gov.hmcts.reform.divorce.ccd.event.SolicitorUpdate.SOLICITOR_UPDATE;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class SolicitorUpdateTest {

    private final SolicitorUpdate solicitorUpdate = new SolicitorUpdate();

    private final EventBuildingMockUtil eventBuildingMockUtil = new EventBuildingMockUtil().mockEventBuilding();
    private final ConfigBuilder<CaseData, State, UserRole> configBuilder = eventBuildingMockUtil.getConfigBuilder();
    private final EventTypeBuilder<CaseData, UserRole, State> eventTypeBuilder = eventBuildingMockUtil.getEventTypeBuilder();
    private final Event.EventBuilder<CaseData, UserRole, State> eventBuilder = eventBuildingMockUtil.getEventBuilder();
    private final FieldCollection.FieldCollectionBuilder<CaseData, Event.EventBuilder<CaseData, UserRole, State>>
        fieldCollectionBuilder = eventBuildingMockUtil.getFieldCollectionBuilder();
    private final Field.FieldBuilder fieldBuilder = eventBuildingMockUtil.getFieldBuilder();

    @Test
    void shouldBuildSolicitorUpdateEventWithConfigBuilder() {

        solicitorUpdate.applyTo(configBuilder);

        verify(configBuilder).event(SOLICITOR_UPDATE);
        verify(eventTypeBuilder).initialState(SOTAgreementPayAndSubmitRequired);
        verify(eventBuilder).name("Apply for a divorce");
        verify(eventBuilder).description("Apply for a divorce");
        verify(eventBuilder).displayOrder(1);
        verify(eventBuilder).showSummary();
        verify(eventBuilder).endButtonLabel("Save Petition");
        verify(eventBuilder).explicitGrants();
        verify(eventBuilder).grant("CRU", CASEWORKER_DIVORCE_SOLICITOR);
        verify(eventBuilder).grant("RU", CASEWORKER_DIVORCE_SUPERUSER);
        verify(eventBuilder).grant(
            "R",
            CASEWORKER_DIVORCE_COURTADMIN_BETA,
            CASEWORKER_DIVORCE_COURTADMIN,
            CASEWORKER_DIVORCE_COURTADMIN_LA);
        verify(eventBuilder, times(1)).fields();

        verify(fieldCollectionBuilder).page("SolAboutTheSolicitor");
        verify(fieldCollectionBuilder).pageLabel("About the Solicitor");
        verify(fieldCollectionBuilder).field("LabelSolAboutEditingApplication-AboutSolicitor");
        verify(fieldBuilder, times(5)).readOnly();
        verify(fieldBuilder, times(3)).label("You can make changes at the end of your application.");
        verify(fieldBuilder, times(4)).showSummary(false);
        verify(fieldBuilder, times(8)).done();
        verify(fieldCollectionBuilder).field("LabelSolAboutTheSolPara-1");
        verify(fieldBuilder).label("Please note that the information provided will be used as evidence by the court to "
            + "decide if the petitioner is entitled to legally end their marriage. **A copy of this form is sent to the "
            + "respondent**");
        verify(fieldCollectionBuilder, times(18)).field(any(TypedPropertyGetter.class), eq(Mandatory), eq(true));

        verify(fieldCollectionBuilder).page("SolAboutThePetitioner");
        verify(fieldCollectionBuilder).pageLabel("About the petitioner");
        verify(fieldCollectionBuilder).field("LabelSolAboutEditingApplication-AboutPetitioner");
        verify(fieldCollectionBuilder).field("LabelSolAboutThePetPara-2");
        verify(fieldBuilder).label("About the petitioner");
        verify(fieldBuilder, times(2)).showCondition("D8PetitionerNameDifferentToMarriageCert=\"Yes\"");
        verify(fieldCollectionBuilder, times(3)).field(any(TypedPropertyGetter.class));
        verify(fieldBuilder, times(2)).mandatory();
        verify(fieldBuilder, times(3)).showSummary();
        verify(fieldBuilder).showCondition("D8PetitionerNameChangedHow=\"other\"");
        verify(fieldCollectionBuilder, times(2)).field(any(TypedPropertyGetter.class), eq(Optional), eq(true));

        verify(fieldCollectionBuilder).page("SolAboutTheRespondent");
        verify(fieldCollectionBuilder).pageLabel("About the respondent");
        verify(fieldCollectionBuilder).field("LabelSolAboutEditingApplication-AboutRespondent");
        verify(fieldBuilder).optional();
        verify(fieldBuilder).showCondition("D8RespondentNameAsOnMarriageCertificate=\"Yes\"");

        verifyNoMoreInteractions(configBuilder, eventTypeBuilder, eventBuilder, fieldCollectionBuilder);
    }
}