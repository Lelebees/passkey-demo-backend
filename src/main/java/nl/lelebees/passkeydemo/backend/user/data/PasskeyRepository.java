package nl.lelebees.passkeydemo.backend.user.data;

import nl.lelebees.passkeydemo.backend.user.domain.Passkey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasskeyRepository extends JpaRepository<Passkey, byte[]> {
}
