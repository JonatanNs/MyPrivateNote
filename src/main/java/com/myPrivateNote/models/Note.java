package com.myPrivateNote.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name = "notes")
@Data
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Utilisation de Long (au lieu de long)

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Clé étrangère vers l'utilisateur
    private User user;

    @Temporal(TemporalType.TIMESTAMP) // Spécifie que c'est une date/heure complète
    @Column(nullable = false, updatable = false)
    private Date date = new Date(); // Date par défaut à la création

    @Column(nullable = false, length = 100) // Limite la taille
    private String category;

    @Column(nullable = false, length = 255) // Un titre peut être plus long qu'une catégorie
    private String title;

    @Lob // Utilisé pour stocker de grandes chaînes de texte
    @Column(nullable = false)
    private String note;
}
