package tsystems.gaiax.onboarding.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import tsystems.gaiax.onboarding.entities.FrRequest;
import tsystems.gaiax.onboarding.entities.FrRequestTypeEntity;

import java.util.Optional;

public interface FrRequestDao extends JpaRepository<FrRequest, Long> {
  Optional<FrRequest> findByEmailAndRequestType(@NonNull String email, @NonNull FrRequestTypeEntity requestType);

  @Override
  @NonNull
  Optional<FrRequest> findById(@NonNull Long aLong);
}
