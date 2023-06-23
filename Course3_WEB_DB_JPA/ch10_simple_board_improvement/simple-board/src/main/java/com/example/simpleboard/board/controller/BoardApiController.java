package com.example.simpleboard.board.controller;

import com.example.simpleboard.board.db.BoardEntity;
import com.example.simpleboard.board.model.BoardDto;
import com.example.simpleboard.board.model.BoardRequest;
import com.example.simpleboard.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/board")
@RequiredArgsConstructor
public class BoardApiController {
    private final BoardService boardService;

    @PostMapping("")
    public BoardDto create(@Valid @RequestBody BoardRequest boardRequest) {
        return boardService.create(boardRequest);
    }

    @GetMapping("/id/{id}")
    public BoardDto view(@PathVariable Long id) {
        BoardDto boardDto = boardService.view(id);
        return boardDto;
    }
}
