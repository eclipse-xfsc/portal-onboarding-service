package tsystems.gaiax.onboarding.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tsystems.gaiax.onboarding.entities.FrRequestTypeEntity;

import java.util.Optional;

public interface FrRequestTypeDao extends JpaRepository<FrRequestTypeEntity, Long> {
    Optional<FrRequestTypeEntity> findByName(String name);
}
