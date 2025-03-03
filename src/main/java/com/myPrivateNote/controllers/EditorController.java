package com.myPrivateNote.controllers;

import com.myPrivateNote.configurations.UserInterceptor;
import com.myPrivateNote.models.Note;
import com.myPrivateNote.models.User;
import com.myPrivateNote.repository.NoteRepository;
import com.myPrivateNote.repository.UserRepository;
import com.myPrivateNote.utils.NoteUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class EditorController {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final UserInterceptor userInterceptor;

    @PostMapping("/save-note")
    public ResponseEntity<?> saveNote(@Valid @RequestParam("note") String content, Principal principal) {
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "La note ne doit pas être vide"));
        }

        // Récupération de l'utilisateur authentifié
        User userEntity = userInterceptor.getAuthenticatedUser(principal);

        // Nettoyage et traitement de la note
        String sanitizedContent = NoteUtils.sanitizeHtml(content);
        String extractedTitle = NoteUtils.extractTitle(sanitizedContent);
        List<String> links = NoteUtils.extractLinks(sanitizedContent);
        List<String> videos = NoteUtils.extractVideos(sanitizedContent);
        boolean hasCode = NoteUtils.containsCode(sanitizedContent);

        // Sauvegarde de la note
        Note note = new Note();
        // Si le titre est vide, utiliser un titre par défaut
        if (extractedTitle == null || extractedTitle.isBlank()) {
            extractedTitle = "Note sans titre";
        }
        note.setTitle(extractedTitle);
        // Si la note est vide (aucun texte, juste une vidéo), mettre un placeholder
        if (sanitizedContent == null || sanitizedContent.isBlank()) {
            sanitizedContent = "<p></p>";
        }

        if (extractedTitle.length() > 100) { // Adapte à la taille max de ta colonne
            extractedTitle = extractedTitle.substring(0, 100);
        }

        note.setNote(sanitizedContent);
        note.setTitle(extractedTitle);
        note.setUser(userEntity);
        noteRepository.save(note);

        return ResponseEntity.ok(Map.of(
                "message", "Nouvelle note enregistrée !",
                "id", note.getId(),
                "title", extractedTitle,
                "links", links,
                "videos", videos,
                "containsCode", hasCode,
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

    @PostMapping("/update-note")
    public ResponseEntity<Map<String, Object>> updateNote(
            @RequestParam("note_id") Long noteId,
            @RequestParam("content") String content,
            Principal principal) {

        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "La note ne doit pas être vide"));
        }

        // Récupération de l'utilisateur authentifié
        User userEntity = userInterceptor.getAuthenticatedUser(principal);

        // Récupération de la note existante
        Note note = noteRepository.findById(noteId).orElse(null);
        if (note == null || !note.getUser().equals(userEntity)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Note introuvable ou accès non autorisé"));
        }

        // Nettoyage et traitement de la note
        String sanitizedContent = NoteUtils.sanitizeHtml(content);
        String extractedTitle = NoteUtils.extractTitle(sanitizedContent);
        List<String> links = NoteUtils.extractLinks(sanitizedContent);
        List<String> videos = NoteUtils.extractVideos(sanitizedContent);
        boolean hasCode = NoteUtils.containsCode(sanitizedContent);

        // Mise à jour de la note existante
        note.setTitle((extractedTitle == null || extractedTitle.isBlank()) ? "Note sans titre" : extractedTitle);
        note.setNote(sanitizedContent.isBlank() ? "<p></p>" : sanitizedContent);
        note.setDate(new Date());
        noteRepository.save(note);

        return ResponseEntity.ok(Map.of(
                "message", "Note mise à jour avec succès !",
                "id", note.getId(),
                "title", note.getTitle(),
                "links", links,
                "videos", videos,
                "containsCode", hasCode,
                "date", new Date()
        ));
    }
}
