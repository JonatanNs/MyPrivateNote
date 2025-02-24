package com.myPrivateNote.controllers;

import com.myPrivateNote.models.User;
import com.myPrivateNote.repository.NoteRepository;
import com.myPrivateNote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MyNotesController {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/mesNotes")
    public String myNotes(Principal user,
                          @AuthenticationPrincipal OidcUser oidcUser,
                          Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("Aucun utilisateur authentifié !");
            return "redirect:/connexion";
        }

        String username = extractUsername(user);
        String email = extractEmail(user);
        String picture = extractPicture(user);

        // Vérifier si un utilisateur avec cet email existe déjà
        User userLogin = (email != null) ? userRepository.findByEmail(email) : null;

        if (userLogin == null) {
            userLogin = userRepository.findByUsername(username);
        }

        // Si aucun utilisateur n'existe, en créer un nouveau pour Google
        if (userLogin == null && email != null) {
            userLogin = createUser(username, email);
        }

        if (userLogin != null) {
            model.addAttribute("name", userLogin.getUsername());
            model.addAttribute("email", userLogin.getEmail());
            model.addAttribute("notes", noteRepository.findByUser(userLogin));
        }

        if (picture != null) {
            model.addAttribute("pictureGoogle", picture);
        }
        return "mesNotes";
    }

    // Extraction du nom d'utilisateur
    private String extractUsername(Principal user) {
        if (user instanceof OAuth2AuthenticationToken oauthToken) {
            Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();
            return (String) userAttributes.get("name");
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }

        return null;
    }

    // Extraction de l'email
    private String extractEmail(Principal user) {
        if (user instanceof OAuth2AuthenticationToken oauthToken) {
            Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();
            return (String) userAttributes.get("email");
        }

        return null; // Si besoin, ajouter la gestion des emails pour les comptes locaux
    }

    // Extraction de l'image de profil (Google OAuth)
    private String extractPicture(Principal user) {
        if (user instanceof OAuth2AuthenticationToken oauthToken) {
            Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();
            return (String) userAttributes.get("picture");
        }
        return null;
    }


    private String generateRandomPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    // Création d'un nouvel utilisateur Google
    private User createUser(String username, String email) {
        User newUser = new User();

        String randomPassword = generateRandomPassword();
        newUser.setPassword(passwordEncoder.encode(randomPassword));
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setRole("ROLE_USER");
        return userRepository.save(newUser);
    }
}