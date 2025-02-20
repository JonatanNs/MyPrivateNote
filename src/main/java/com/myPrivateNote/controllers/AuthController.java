package com.myPrivateNote.controllers;

import com.myPrivateNote.configurations.JwtUtils;
import com.myPrivateNote.models.User;
import com.myPrivateNote.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Ce nom d'utilisateur est déjà pris. Veuillez en choisir un autre.");
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Cette e-mail est déjà utilisée. Connectez-vous!");
        }

        // Vérification du mot de passe avec regex
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>?/])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};':\",.<>?/]{10,}$";
        if (!Pattern.matches(regex, user.getPassword())) {
            return ResponseEntity.badRequest().body("Le mot de passe doit contenir au moins 10 caractères, une majuscule, un chiffre et un caractère spécial.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        return ResponseEntity.ok("Utilisateur enregistré avec succès !");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@Valid @RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            String token = jwtUtils.generateToken(user.getUsername());

            response.put("token", token);
            response.put("type", "Bearer");
            response.put("message", "Connexion réussie");

            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            log.error("Erreur d'authentification : {}", ex.getMessage());

            response.put("message", "Nom d'utilisateur ou mot de passe invalide.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/*")
    public String getUserInfo(Principal user,
                              @AuthenticationPrincipal OidcUser oidcUser,
                              Model model){
        String userInfo = "";

        if (user instanceof UsernamePasswordAuthenticationToken) {
            userInfo = getUsernamePasswordLoginInfo(user).toString();
        } else if (user instanceof OAuth2AuthenticationToken) {
            userInfo = getOauth2LoginInfo(user, oidcUser).toString();
        }
        model.addAttribute("userInfo", userInfo);
        return "userInfo";
    }

    private StringBuffer getUsernamePasswordLoginInfo(Principal user){
        StringBuffer usernameInfo = new StringBuffer();
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) user;
        if(token.isAuthenticated()){
            org.springframework.security.core.userdetails.User u = (org.springframework.security.core.userdetails.User) token.getPrincipal();
            usernameInfo.append("Welcom, " + u.getUsername());
        } else {
            usernameInfo.append("NA");
        }
        return usernameInfo;
    }

    private StringBuffer getOauth2LoginInfo(Principal user, OidcUser oidcUser) {
        StringBuffer protectedInfo = new StringBuffer();

        OAuth2AuthenticationToken authToken = ((OAuth2AuthenticationToken) user);
        OAuth2AuthorizedClient authClient = this.authorizedClientService
                .loadAuthorizedClient(authToken.getAuthorizedClientRegistrationId(), authToken.getName());
        if (authToken.isAuthenticated()) {

            Map<String, Object> userAttributes = ((DefaultOAuth2User) authToken.getPrincipal()).getAttributes();

            String userToken = authClient.getAccessToken().getTokenValue();
            protectedInfo.append("Welcome, " + userAttributes.get("name") + "<br><br>");
            protectedInfo.append("e-mail: " + userAttributes.get("email") + "<br><br>");
            protectedInfo.append("Access Token: " + userToken + "<br><br>");

            if (oidcUser != null) {
                OidcIdToken idToken = oidcUser.getIdToken();
                if (idToken != null) {
                    protectedInfo.append("idToken value: " + idToken.getTokenValue() + "<br><br>");
                    protectedInfo.append("Token mapped values <br><br>");
                    Map<String, Object> claims = idToken.getClaims();
                    for (String key : claims.keySet()) {
                        protectedInfo.append("  " + key + ": " + claims.get(key) + "<br>");
                    }
                }
            }
        } else {
            protectedInfo.append("NA");
        }
        return protectedInfo;
    }
}