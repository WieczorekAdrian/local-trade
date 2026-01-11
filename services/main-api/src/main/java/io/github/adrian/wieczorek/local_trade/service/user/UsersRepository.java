package io.github.adrian.wieczorek.local_trade.service.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, Integer> {
  Optional<UsersEntity> findByEmail(String email);

  Optional<UsersEntity> findByName(String name);

  Optional<UsersEntity> findByUserId(UUID userId);
}
