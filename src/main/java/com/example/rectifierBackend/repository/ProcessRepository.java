package com.example.rectifierBackend.repository;

import com.example.rectifierBackend.model.Process;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessRepository extends JpaRepository<Process, Long> {
    Optional<Process> findById(long id);
    List<Process> findAll();
//    List<Process> findByOrderId(long id);
    List<Process> findByStartTimestampBetween(Timestamp after, Timestamp before);
    List<Process> findByStopTimestampBetween(Timestamp after, Timestamp before);
    List<Process> findByStartTimestampLessThanAndStopTimestampGreaterThan(Timestamp t1, Timestamp t2);
    List<Process> findByInsertCodeIgnoreCaseContainingAndElementNameIgnoreCaseContainingAndDrawingNumberIgnoreCaseContainingAndOrderNumberIgnoreCaseContainingAndMonterIgnoreCaseContainingAndStopTimestampGreaterThanAndStartTimestampLessThan(String insertCode, String elementName, String drawingNumber, String orderNumber, String monter, Timestamp timeFrom, Timestamp timeTo);
    Process save(Process process);
    long deleteById(long id);
}
