package com.example.demo.controller

import com.example.jooq.tables.records.AuthorsRecord
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ViewController {

    @GetMapping("")
    fun index(model: Model): String {
        model.addAttribute("authors", AuthorsRecord())
        return "index"
    }
}