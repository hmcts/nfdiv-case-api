package uk.gov.hmcts.divorce.notification.pin;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
public class PinGenerationService {

    private static final String ALLOWED_CHARS = "ABCDEFGJKLMNPRSTVWXYZ23456789";

    public String generatePin() {
        final char[] allowedChars = ALLOWED_CHARS.toCharArray();
        return RandomStringUtils.random(8, allowedChars);
    }
}
