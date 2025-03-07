package com.myPrivateNote.controllers;

import com.myPrivateNote.configurations.UserInterceptor;
import com.myPrivateNote.models.User;
import com.myPrivateNote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserInterceptor userInterceptor;
    private final PasswordEncoder passwordEncoder;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", "Le fichier est trop volumineux ! (max 5MB)"));
    }

    @PostMapping(value = "/update-user-img", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> showUpdateUserImg(
            @RequestParam("fileImg") MultipartFile file,
            Principal user) {

        Map<String, Object> response = new HashMap<>();
        User userEntity = userInterceptor.getAuthenticatedUser(user);

        if (file.isEmpty()) {
            response.put("message", "Aucun fichier sélectionné !");
            return ResponseEntity.status(400).body(response);
        }

        try {
            // Utiliser un chemin absolu pour éviter le dossier temporaire de Tomcat
            String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs(); // Crée le dossier s'il n'existe pas
            }

            // Nettoyer le nom du fichier pour éviter les erreurs
            String fileName = file.getOriginalFilename().replaceAll("\\s+", "_");
            String filePath = uploadDir + fileName;
            File destinationFile = new File(filePath);

            // Vérifier et supprimer le fichier existant
            if (destinationFile.exists()) {
                destinationFile.delete();
            }

            // Sauvegarder le fichier
            file.transferTo(destinationFile);

            // Stocker le chemin correct dans la base de données
            userEntity.setImgUser_url("/uploads/" + fileName);
            userRepository.save(userEntity);

            response.put("message", "Photo de profil mise à jour avec succès !");
            response.put("fileUrl", "/uploads/" + fileName);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Erreur lors de l'enregistrement de l'image : " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
