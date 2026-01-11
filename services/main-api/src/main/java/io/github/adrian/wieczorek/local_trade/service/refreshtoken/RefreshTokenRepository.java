package io.github.adrian.wieczorek.local_trade.service.refreshtoken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Integer> {
  Optional<RefreshTokenEntity> findByToken(String token);

  @Modifying
  @Query("DELETE FROM RefreshTokenEntity r WHERE r.token = :token")
  void deleteByToken(@Param("token") String token);
}
