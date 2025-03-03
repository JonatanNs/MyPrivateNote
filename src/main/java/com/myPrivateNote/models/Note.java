package com.myPrivateNote.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name = "notes")
@Data
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date date = new Date();

    @Column(nullable = false, length = 255)
    private String title;

    @Lob // Utilisé pour stocker de grandes chaînes de texte
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La note ne doit pas être vide")
    private String note;
}
