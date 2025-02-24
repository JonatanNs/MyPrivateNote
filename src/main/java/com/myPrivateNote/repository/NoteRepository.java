package com.myPrivateNote.repository;

import com.myPrivateNote.models.Note;
import com.myPrivateNote.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUser(User user);
}
