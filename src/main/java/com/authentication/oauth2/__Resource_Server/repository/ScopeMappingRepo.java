package com.authentication.oauth2.__Resource_Server.repository;

import com.authentication.oauth2.__Resource_Server.entity.ApiRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
@Repository
public interface ScopeMappingRepo extends JpaRepository<ApiRule, Serializable> {
}
