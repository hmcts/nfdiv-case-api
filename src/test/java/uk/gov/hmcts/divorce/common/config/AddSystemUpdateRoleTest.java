package uk.gov.hmcts.divorce.common.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.AddSystemUpdateRole;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.model.Letter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.FILE_NAME_DATE_FORMATTER;

@ExtendWith(MockitoExtension.class)
public class AddSystemUpdateRoleTest {

    @InjectMocks
    private AddSystemUpdateRole addSystemUpdateRole;

    @Test
    public void shouldAddSystemUpdateRoleWhenEnvironmentIsAat() throws Exception {
        List<UserRole> actualRoles =
            withEnvironmentVariable("ENVIRONMENT", "aat")
                .execute(() -> addSystemUpdateRole.addIfConfiguredForEnvironment(List.of(CITIZEN))
                );

        assertThat(actualRoles).containsExactlyInAnyOrder(CITIZEN, SYSTEMUPDATE);
    }

    @Test
    public void shouldReturnTrueWhenEnvironmentIsAat() throws Exception {
        boolean isEnvironmentAat =
            withEnvironmentVariable("ENVIRONMENT", "aat")
                .execute(() -> addSystemUpdateRole.isEnvironmentAat()
                );

        assertThat(isEnvironmentAat).isTrue();
    }

    @Test
    public void testDateFiltering() throws Exception {

        var divorceDoc1 = DivorceDocument.builder().documentFileName("mini-application--1631869072513040-2021-09-16:13:25.pdf").build();
        var divorceDoc2 = DivorceDocument.builder().documentFileName("mini-application--1631869072513040-2021-09-16:13:25.pdf").build();
        var  letter1 = new Letter(divorceDoc1,1);
        var  letter2 = new Letter(divorceDoc2,1);

        final List<Letter> currentAosLetters = Stream.of(letter1,letter2)
            .filter(letter -> letter.getDivorceDocument().getDocumentFileName().contains(LocalDate.now().format(FILE_NAME_DATE_FORMATTER)))
            .collect(toList());

        System.out.println(currentAosLetters.size());
    }
}
