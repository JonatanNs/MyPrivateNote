package com.myPrivateNote.controllers;

import com.myPrivateNote.configurations.UserInterceptor;
import com.myPrivateNote.models.Note;
import com.myPrivateNote.models.User;
import com.myPrivateNote.repository.NoteRepository;
import com.myPrivateNote.services.ImageStorageService;
import com.myPrivateNote.utils.NoteUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class EditorController {

    private final NoteRepository noteRepository;
    private final UserInterceptor userInterceptor;
    private final ImageStorageService imageStorageService;

    @PostMapping("/save-note")
    public ResponseEntity<?> saveNote(@Valid @RequestParam("note") String content, Principal principal) throws IOException {
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "La note ne doit pas être vide"));
        }
        // Récupération de l'utilisateur authentifié
        User userEntity = userInterceptor.getAuthenticatedUser(principal);
        // Nettoyage et traitement de la note
        String sanitizedContent = NoteUtils.sanitizeHtml(content);
        String extractedTitle = NoteUtils.extractTitle(sanitizedContent);
        List<String> links = NoteUtils.extractLinks(sanitizedContent);
        boolean hasCode = NoteUtils.containsCode(sanitizedContent);

        // Sauvegarde de la note
        Note note = new Note();
        // Si le titre est vide, utiliser un titre par défaut
        if (extractedTitle == null || extractedTitle.isBlank()) {
            extractedTitle = "Note sans titre";
        }

        if (extractedTitle.length() > 100) { // Adapte à la taille max de ta colonne
            extractedTitle = extractedTitle.substring(0, 100);
        }

        note.setTitle(extractedTitle);
        note.setNote(sanitizedContent);
        note.setUser(userEntity);
        noteRepository.save(note);

        return ResponseEntity.ok(Map.of(
                "message", "Nouvelle note enregistrée !",
                "id", note.getId(),
                "title", extractedTitle,
                "links", links,
                "containsCode", hasCode,
                "note", sanitizedContent,
                "date", note.getDate()
        ));
    }

    @PostMapping("/update-note")
    public ResponseEntity<Map<String, Object>> updateNote(
            @RequestParam("note_id") Long noteId,
            @RequestParam("content") String content,
            Principal principal) throws IOException {

        // Vérifier que le contenu de la note n'est pas vide
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "La note ne doit pas être vide"));
        }

        // Récupérer la note existante de l'utilisateur
        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Aucune note trouvée pour cet utilisateur"));
        }

        // Nettoyer et traiter le contenu de la note
        String sanitizedContent = NoteUtils.sanitizeHtml(content);
        String extractedTitle = NoteUtils.extractTitle(sanitizedContent);
        List<String> links = NoteUtils.extractLinks(sanitizedContent);
        boolean hasCode = NoteUtils.containsCode(sanitizedContent);

        // Si le titre est vide, utiliser un titre par défaut
        if (extractedTitle == null || extractedTitle.isBlank()) {
            extractedTitle = "Note sans titre";
        }

        // Si la note est vide, conserver l'ancienne version
        if (sanitizedContent == null || sanitizedContent.isBlank()) {
            sanitizedContent = note.getNote();
        }

        // Limiter la longueur du titre
        if (extractedTitle.length() > 100) { // Limite de la colonne titre
            extractedTitle = extractedTitle.substring(0, 100);
        }

        // Sauvegarder la note mise à jour
        note.setTitle(extractedTitle);
        note.setNote(sanitizedContent);
        noteRepository.save(note);

        // Retourner une réponse avec les informations mises à jour, sans les images
        return ResponseEntity.ok(Map.of(
                "message", "Note mise à jour avec succès !" ,
                "id", note.getId(),
                "title", extractedTitle,
                "links", links,
                "containsCode", hasCode,
                "note", sanitizedContent,
                "date", note.getDate()
        ));
    }

    @PostMapping("/delete-note")
    public ResponseEntity<String> deleteNote(
            @RequestParam("id") Long id
    ) {

        // Vérifie si la note existe et appartient à l'utilisateur
        Optional<Note> noteOptional = noteRepository.findById(id);

        if (noteOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Note introuvable ou non autorisée.");
        }

        noteRepository.deleteById(id);
        return ResponseEntity.ok("Note supprimée avec succès.");
    }
}
