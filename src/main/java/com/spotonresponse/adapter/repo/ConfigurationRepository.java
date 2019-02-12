package com.spotonresponse.adapter.repo;

import com.spotonresponse.adapter.model.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface ConfigurationRepository extends JpaRepository<Configuration, String> {

    /*
    @Query("SELECT config FROM Configuration config WHERE config.id=(:id)")
    List<Configuration> findById(@Param("id") String id);
    */
}
