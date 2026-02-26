package nl.lelebees.passkeydemo.backend.domain;

import nl.lelebees.passkeydemo.backend.user.domain.Email;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class EmailTest {

    // ProtonMail addresses are case-sensitive. A capital letter is therefore part of valid email syntax.
    @ParameterizedTest
    @ValueSource(strings = {"Test@test.com", "test@test.com", "gerda.pizzabal@test.company.com"})
    void correctEmailDoesNotGiveParseException(String email){
        assertDoesNotThrow(() -> new Email(email));
    }

}