package com.spotonresponse.adapter.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user")
public class User {
    @Id
    private String username;
    private String password;
    private String type;
}
