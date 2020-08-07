package com.example.rectifierBackend.repository;

import com.example.rectifierBackend.model.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {
    Optional<Sample> findById(Long id);
    List<Sample> findAll();
    List<Sample> findAllByProcessIdOrderByTimestampAsc(Long processId);
    Sample save(Sample sample);
    void deleteById(Long id);
}
