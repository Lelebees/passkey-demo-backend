package nl.lelebees.passkeydemo.backend.domain;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class Email {
    private String value;

    public Email(String value) throws IncorrectEmailFormatException {
        value = value.strip();
        if (!value.matches(
                """
                        (?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])
                        """)) {
            throw new IncorrectEmailFormatException("%s is not a valid email format");
        }
        this.value = value;
    }

    protected Email() {

    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(this.value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
