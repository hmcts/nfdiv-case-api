package uk.gov.hmcts.divorce.notification.pin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class PinGenerationServiceTest {

    @InjectMocks
    private PinGenerationService pinGenerationService;

    @Test
    public void generatePinTest() {
        String result = pinGenerationService.generatePin();

        assertThat(result.length(), is(8));
        assertFalse(result.contains("I"));
        assertFalse(result.contains("O"));
        assertFalse(result.contains("U"));
        assertFalse(result.contains("0"));
        assertFalse(result.contains("1"));
    }
}
