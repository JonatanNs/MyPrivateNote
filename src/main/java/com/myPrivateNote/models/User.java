package com.myPrivateNote.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true, length = 555)
    private String imgUser_url;

    @Column(nullable = false)
    private String role;

}
