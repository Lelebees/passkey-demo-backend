package nl.lelebees.passkeydemo.backend.data;

import nl.lelebees.passkeydemo.backend.domain.Passkey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasskeyRepository extends JpaRepository<Passkey, byte[]> {
}
