package za.co.absa.subatomic.infrastructure.project.view.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BitbucketProjectRepository
        extends JpaRepository<BitbucketProjectEntity, Long> {

    BitbucketProjectEntity findByKey(String key);
}
