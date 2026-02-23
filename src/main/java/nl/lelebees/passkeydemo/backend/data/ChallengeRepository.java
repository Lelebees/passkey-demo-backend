package nl.lelebees.passkeydemo.backend.data;

import nl.lelebees.passkeydemo.backend.domain.ChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChallengeRepository extends JpaRepository<ChallengeEntity, String> {
}
