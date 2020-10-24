package com.example.rectifierBackend.repository;

import com.example.rectifierBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(long id);
    Optional<User> findByUsername(String username);
    User save(User user);
    Long deleteById(long id);
    Long deleteByUsername(String username);
    Boolean existsByUsername(String username);
}
