package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.citizen.notification.JointApplicationOverdueNotification;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;

@ExtendWith(MockitoExtension.class)
public class SystemNotifyApplicant1ApplyForConditionalOrderTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private JointApplicationOverdueNotification jointApplicationOverdueNotification;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private SystemNotifyApplicant1ApplyForConditionalOrder systemNotifyApplicant1ApplyForConditionalOrder;

    @Test
    void shouldSendEmailForConditionalOrder() {

    }
}
