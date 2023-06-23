package com.example.simpleboard.board.service;

import com.example.simpleboard.board.db.BoardEntity;
import com.example.simpleboard.board.db.BoardRepository;
import com.example.simpleboard.board.model.BoardDto;
import com.example.simpleboard.board.model.BoardRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardConverter boardConverter;

    public BoardDto create(BoardRequest boardRequest) {
        BoardEntity boardEntity = BoardEntity.builder()
                .boardName(boardRequest.getBoardName())
                .status("REGISTERED")
                .build();
        BoardEntity saveEntity = boardRepository.save(boardEntity);
        return boardConverter.toDto(saveEntity);
    }

    public BoardDto view(Long id) {
        BoardEntity boardEntity = boardRepository.findById(id).get();
        return boardConverter.toDto(boardEntity);
    }
}
