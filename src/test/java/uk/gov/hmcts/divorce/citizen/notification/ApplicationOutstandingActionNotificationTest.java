package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.SupportingDocumentType;
import uk.gov.hmcts.divorce.common.model.WhoDivorcing;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SERVICE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAPERS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.UNION_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.NAME_CHANGE_PROOF;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ApplicationOutstandingActionNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private ApplicationOutstandingActionNotification notification;

    @Test
    void shouldCallSendEmailWithSubmissionResponseDate() {
        CaseData data = caseData();
        data.setDivorceWho(WhoDivorcing.HUSBAND);

        Set<SupportingDocumentType> docs = new HashSet<>();
        docs.add(SupportingDocumentType.UNION_CERTIFICATE);
        docs.add(SupportingDocumentType.NAME_CHANGE_PROOF);
        data.setCannotUploadSupportingDocument(docs);

        notification.send(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICATION_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, "1234-5678-9012-3456"),
                hasEntry(SERVICE, "divorce"),
                hasEntry(PARTNER, "husband"),
                hasEntry(PAPERS, "divorce papers"),
                hasEntry(MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriage"),
                hasEntry(UNION_CERTIFICATE, "yes"),
                hasEntry(NAME_CHANGE_PROOF, "yes")
            )),
            eq(ENGLISH)
        );
        verify(commonContent).apply(any(), eq(data));
    }

}
