package com.example.simpleboard.post.db;

import com.example.simpleboard.board.db.BoardEntity;
import com.example.simpleboard.reply.db.ReplyEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity(name = "post")
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    private BoardEntity board;  // board + _id
    private String userName;
    private String password;
    private String email;
    private String status;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime postedAt;
    @OneToMany(mappedBy = "post")
    @Builder.Default
    private List<ReplyEntity> replyList = new ArrayList<>();

}
