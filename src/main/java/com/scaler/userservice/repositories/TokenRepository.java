package com.scaler.userservice.repositories;

import com.scaler.userservice.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token>
    findByValueAndDeletedAndExpiryAtGreaterThan(String value, Boolean deleted, Date expiry);

    Optional<Token> findByValueAndDeleted(String value, Boolean deleted);
}
