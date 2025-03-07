package com.myPrivateNote.controllers;

import com.myPrivateNote.models.Note;
import com.myPrivateNote.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final NoteRepository noteRepository;

    @GetMapping("/*")
    public String home(Model model) {
        return "home";
    }

    @GetMapping("/connexion")

    public String showLogin(Model model){
        return "login";
    }

    @GetMapping("/inscription")
    public String showRegister(Model model){
        return "register";
    }

    @GetMapping("/profil")
    public String showProfil(Model model){
        return "profil";
    }

    @GetMapping("/nouvelle-note")
    public String showNewNote(Model model){
        return "myNote/newNote";
    }

    @GetMapping("/visuel-note")
    public String showLookNote(@RequestParam("id") Long id, Model model) {
        // Récupérer la note par son ID
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note introuvable"));

        model.addAttribute("note", note); // Ajout de la note

        return "myNote/viewNote";
    }

    @GetMapping("/note")
    public String showNote(@RequestParam("id") Long id, Model model) {

        // Récupérer la note par son ID
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note introuvable"));

        model.addAttribute("note", note); // Ajout de la note
        return "myNote/note";
    }
}
