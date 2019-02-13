package com.spotonresponse.adapter.repo;

import com.spotonresponse.adapter.model.CoreConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface CoreConfigurationRepository extends JpaRepository<CoreConfiguration, String> {}
