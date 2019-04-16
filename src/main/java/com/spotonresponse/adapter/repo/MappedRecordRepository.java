package com.spotonresponse.adapter.repo;

import com.spotonresponse.adapter.model.MappedRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface MappedRecordRepository extends JpaRepository<MappedRecord, Integer> {

    @Query("SELECT r FROM MappedRecord r WHERE r.creator=(:creator)")
    List<MappedRecord> lstRecordByCreator(@Param("creator") String creator);
}
