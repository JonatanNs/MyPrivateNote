package com.myPrivateNote.controllers;

import com.myPrivateNote.models.User;
import com.myPrivateNote.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

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
    public ResponseEntity<?> loginUser(@Valid @RequestBody User user){
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            user.getPassword()
                    )
            );
            return ResponseEntity.ok("Connexion réussie !");
        } catch (AuthenticationException ex){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nom d'utilisateur ou mot de passe invalide.");
        }
    }
}