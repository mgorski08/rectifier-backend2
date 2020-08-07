package com.example.rectifierBackend.repository;

import com.example.rectifierBackend.model.Bath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BathRepository extends JpaRepository<Bath, Long> {
    Optional<Bath> findById(long id);
    List<Bath> findAll();
    List<Bath> findAllByOrderByIdAsc();
    Bath save(Bath bath);
    long deleteById(long id);
}
