package nl.lelebees.passkeydemo.backend.user.data;

import nl.lelebees.passkeydemo.backend.user.domain.Email;
import nl.lelebees.passkeydemo.backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsUserByEmail(Email email);

    Optional<User> findUserByEmail(Email email);
}
