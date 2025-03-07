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
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    @GetMapping("mesNotes")
    public String myNotes(Principal user,
                          @AuthenticationPrincipal OidcUser oidcUser,
                          Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser");

        // Vérifier l'utilisateur s'est connecté via Google OAuth2
        boolean isGoogleUser = isAuthenticated && authentication.getPrincipal() instanceof OAuth2User;
        model.addAttribute("isGoogleUser", isGoogleUser);

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/connexion";
        }

        String usernameGoogle = extractUsername(user);
        String emailGoogle = extractEmail(user);
        String pictureGoogle = extractPicture(user);

        // Vérifier si utilisateur avec cet email existe déjà
        User userLogin = (emailGoogle != null)
                ? userRepository.findByEmail(emailGoogle).orElse(null)
                : null;

        if (userLogin == null) {
            userLogin = userRepository.findByUsername(usernameGoogle);
        }

        // Si aucun utilisateur n'existe, en créer un nouveau pour Google
        if (userLogin == null && emailGoogle != null) {
            userLogin = createUser(usernameGoogle, emailGoogle, user);
        }

        if (userLogin != null) {
            model.addAttribute("name", userLogin.getUsername());
            model.addAttribute("email", userLogin.getEmail());
            model.addAttribute("notes", noteRepository.findByUser(userLogin));
            model.addAttribute("picture", userLogin.getImgUser_url());
            model.addAttribute("isAuthenticated", isAuthenticated);
        }

        if (pictureGoogle != null) {
            model.addAttribute("pictureGoogle", pictureGoogle);
        }

        return "myNote/mesNotes";
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
        return null;
    }

    // Extraction de image de profile (Google OAuth)
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
    private User createUser(String username, String email, Principal user) {
        User newUser = new User();

        String randomPassword = generateRandomPassword();
        String pictureGoogle = extractPicture(user);

        newUser.setPassword(passwordEncoder.encode(randomPassword));
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setImgUser_url(pictureGoogle);
        newUser.setRole("ROLE_USER");
        return userRepository.save(newUser);
    }
}