package com.authentication.oauth2.__Resource_Server.entity;


import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "api_rules")
@Data
public class ApiRule {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(name = "endpoint", nullable = false)
    private String url;
    @Column(name = "httpMethod", nullable = false)
    private String method;
    @Column(name = "rules", columnDefinition = "JSONB")
    private String rulesJson; // Store rules as JSONB
}
