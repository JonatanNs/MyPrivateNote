package com.myPrivateNote.services;

import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final String UPLOAD_DIR = "uploads/"; // Dossier où stocker les image

    public String saveImage(String base64Image) throws IOException {
        if (base64Image.startsWith("data:image")) {
            base64Image = base64Image.split(",")[1]; // Supprime le préfixe "data:image/png;base64,"
        }

        byte[] decodedBytes = Base64.getDecoder().decode(base64Image);
        String fileName = UUID.randomUUID() + ".png"; // Génère un nom unique
        File file = new File(UPLOAD_DIR);
        if (!file.exists()) {
            file.mkdirs(); // Crée le dossier s'il n'existe pas
        }

        File imageFile = new File(UPLOAD_DIR + fileName);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(decodedBytes);
        }

        return "/images/" + fileName; // Retourne l'URL de l'image (à configurer dans ton controller)
    }
}

