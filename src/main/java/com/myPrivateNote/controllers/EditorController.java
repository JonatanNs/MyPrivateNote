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
        List<String> extractedImages = NoteUtils.extractImages(sanitizedContent);
        String previewContent = NoteUtils.removeImages(sanitizedContent);
        List<String> links = NoteUtils.extractLinks(sanitizedContent);
        List<String> videos = NoteUtils.extractVideos(sanitizedContent);
        boolean hasCode = NoteUtils.containsCode(sanitizedContent);

        List<String> storedImageUrls = new ArrayList<>();
        for (String base64Image : extractedImages) {
            String imageUrl = imageStorageService.saveImage(base64Image); // Stocke l'image et récupère l'URL
            sanitizedContent = sanitizedContent.replace(base64Image, imageUrl);
            storedImageUrls.add(imageUrl);
        }
        // Sauvegarde de la note
        Note note = new Note();
        // Si le titre est vide, utiliser un titre par défaut
        if (extractedTitle == null || extractedTitle.isBlank()) {
            extractedTitle = "Note sans titre";
        }
        note.setTitle(extractedTitle);
        // Si la note est vide (aucun texte, juste une vidéo), mettre un placeholder
        if (sanitizedContent == null || sanitizedContent.isBlank()) {
            sanitizedContent = "";
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
                "preview", previewContent,
                "links", links,
                "videos", videos,
                "images", storedImageUrls, // Utiliser la liste mise à jour
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
            Principal principal) throws IOException {

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

        // Récupération des images déjà enregistrées dans la note
        List<String> oldImages = NoteUtils.extractImages(note.getNote());

        // Nettoyage et traitement de la nouvelle note
        String sanitizedContent = NoteUtils.sanitizeHtml(content);
        String extractedTitle = NoteUtils.extractTitle(sanitizedContent);
        List<String> newImages = NoteUtils.extractImages(sanitizedContent);
        List<String> links = NoteUtils.extractLinks(sanitizedContent);
        List<String> videos = NoteUtils.extractVideos(sanitizedContent);
        boolean hasCode = NoteUtils.containsCode(sanitizedContent);

        // Fusionner anciennes et nouvelles images
        List<String> allImages = new ArrayList<>(oldImages);

        for (String base64Image : newImages) {
            if (!oldImages.contains(base64Image)) { // Vérifie si l'image est déjà stockée
                String imageUrl = imageStorageService.saveImage(base64Image);
                sanitizedContent = sanitizedContent.replace(base64Image, imageUrl);
                allImages.add(imageUrl);
            }
        }

        // Remettre les anciennes images si elles ne sont pas dans le nouveau contenu
        for (String oldImage : oldImages) {
            if (!sanitizedContent.contains(oldImage)) {
                sanitizedContent += "<img src='" + oldImage + "'>";
            }
        }

        // Retirer les balises <p> inutiles autour du contenu
        sanitizedContent = sanitizedContent.replaceAll("(?s)<p>(.*?)</p>", "$1");

        // Mise à jour de la note existante
        note.setTitle((extractedTitle == null || extractedTitle.isBlank()) ? "" : extractedTitle);
        note.setNote(sanitizedContent.isBlank() ? "" : sanitizedContent);
        note.setDate(new Date());
        noteRepository.save(note);

        return ResponseEntity.ok(Map.of(
                "message", "Note mise à jour avec succès !",
                "id", note.getId(),
                "title", note.getTitle(),
                "links", links,
                "videos", videos,
                "images", allImages,
                "containsCode", hasCode,
                "date", new Date()
        ));
    }
}
