package com.myPrivateNote.configurations;

import com.myPrivateNote.models.User;
import com.myPrivateNote.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
                    && !authentication.getPrincipal().equals("anonymousUser");

            String userName = null;
            String imgUrl = null;
            String imgUrlGoogle = null;
            boolean isGoogleUser = false;

            if (isAuthenticated) {
                Object principal = authentication.getPrincipal();

                // Si connexion via un compte classique
                if (principal instanceof UserDetails userDetails) {
                    userName = userDetails.getUsername();
                    User userLogin = userRepository.findByUsername(userName);
                    if (userLogin != null) {
                        imgUrl = userLogin.getImgUser_url();  // Image de la base de données
                    }
                }

                // Si connexion via Google OAuth2
                if (principal instanceof OAuth2User oauth2User) {
                    String email = oauth2User.getAttribute("email"); // Vérifie via l'email
                    userName = oauth2User.getAttribute("name");
                    imgUrlGoogle = oauth2User.getAttribute("picture");
                    isGoogleUser = true;

                    // Vérifie si l'utilisateur existe déjà en base de données
                    Optional<User> optionalUser = userRepository.findByEmail(email);
                    if (optionalUser.isPresent()) {
                        User existingUser = optionalUser.get();
                        imgUrl = existingUser.getImgUser_url();  // Priorise l'image de la base de données
                    }
                }
            }

            // Ajout des attributs au modèle
            modelAndView.addObject("isAuthenticated", isAuthenticated);
            modelAndView.addObject("isGoogleUser", isGoogleUser);
            modelAndView.addObject("userName", userName);
            modelAndView.addObject("picture", imgUrl);
            modelAndView.addObject("pictureGoogle", imgUrlGoogle);
        }
    }

    /**
     * Récupère l'utilisateur authentifié en fonction du type d'authentification (OAuth2 ou standard).
     */
    public User getAuthenticatedUser(Principal principal) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur OAuth2 introuvable"));
        }

        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            return userRepository.findByEmail(principal.getName())
                    .or(() -> Optional.ofNullable(userRepository.findByUsername(principal.getName())))
                    .orElseThrow(() -> new RuntimeException("Utilisateur standard introuvable"));
        }

        throw new RuntimeException("Méthode d'authentification inconnue !");
    }
}
