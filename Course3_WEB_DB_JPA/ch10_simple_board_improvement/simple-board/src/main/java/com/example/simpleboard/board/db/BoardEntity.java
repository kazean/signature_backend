package com.example.simpleboard.board.db;

import com.example.simpleboard.post.db.PostEntity;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "board")
public class BoardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String boardName;
    private String status;
    @OneToMany(mappedBy = "board")
    @Where(clause = "status = 'REGISTERED'")
    @org.hibernate.annotations.OrderBy(clause = "id desc")
    @Builder.Default
    private List<PostEntity> postList = new ArrayList<>();
}
