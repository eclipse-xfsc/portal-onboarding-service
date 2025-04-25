package tsystems.gaiax.onboarding.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tsystems.gaiax.onboarding.entities.FrRequestStatusEntity;

import java.util.Optional;

public interface FrRequestStatusDao extends JpaRepository<FrRequestStatusEntity, Long> {
    Optional<FrRequestStatusEntity> findByName(String name);
}
