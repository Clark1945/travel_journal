package org.travel_journal.note_service.controller;

import org.springframework.web.bind.annotation.*;

@RestController("/")
public class NoteController {

    public NoteController() {}


    @PostMapping("/note")
    public String create() {
        return "create";
    }

    @GetMapping("/note")
    public String get() {
        return "get";
    }

    @PutMapping("/note")
    public String update() {
        return "update";
    }

    @DeleteMapping("/note")
    public String delete() {
        return "delete";
    }
}
