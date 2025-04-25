package tsystems.gaiax.onboarding.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import tsystems.gaiax.onboarding.entities.FrRequestAttachment;

import java.util.Optional;

public interface FrRequestAttachmentDao extends JpaRepository<FrRequestAttachment, Long> {
  @NonNull
  Optional<FrRequestAttachment> findByFileNameAndFrRequest_Id(String fileName, Long id);


}
