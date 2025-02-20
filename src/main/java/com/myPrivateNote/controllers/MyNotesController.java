package com.myPrivateNote.controllers;

import com.myPrivateNote.models.User;
import com.myPrivateNote.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MyNotesController {

    private final UserRepository userRepository;

    @GetMapping("/mesNotes")
    public String myNotes(Principal user,
                          @AuthenticationPrincipal OidcUser oidcUser,
                          Model model) {
        if (user instanceof OAuth2AuthenticationToken) {
            Map<String, Object> userAttributes = ((OAuth2AuthenticationToken) user).getPrincipal().getAttributes();

            model.addAttribute("nameGoogle", userAttributes.get("name"));
            model.addAttribute("emailGoogle", userAttributes.get("email"));
            model.addAttribute("pictureGoogle", userAttributes.get("picture"));
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String) {
            // Cas où principal est juste un nom d'utilisateur
            username = (String) principal;
        } else {
            System.out.println("⚠️ Impossible d'identifier le type d'utilisateur !");
            return "mesNotes"; // Évite de planter l'affichage
        }

        System.out.println("✅ Nom d'utilisateur récupéré : " + username);

        User userLogin = userRepository.findByUsername(username);
        if (userLogin != null) {
            model.addAttribute("name", userLogin.getUsername());
            model.addAttribute("email", userLogin.getEmail());
            System.out.println("📌 Informations utilisateur ajoutées au modèle !");
        } else {
            System.out.println("⚠️ Utilisateur non trouvé en base !");
        }
        return "mesNotes";
    }
}