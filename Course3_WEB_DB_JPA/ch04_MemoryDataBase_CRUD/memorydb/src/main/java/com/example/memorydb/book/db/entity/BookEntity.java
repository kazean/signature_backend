package com.example.memorydb.book.entity;

import com.example.memorydb.entity.Entity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookEntity extends Entity {
    private String name;
    private String category;
    private BigDecimal amount;
}
