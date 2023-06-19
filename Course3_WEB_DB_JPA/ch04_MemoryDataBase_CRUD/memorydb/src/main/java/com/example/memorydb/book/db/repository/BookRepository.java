package com.example.memorydb.book.db.repository;

import com.example.memorydb.book.db.entity.BookEntity;
import com.example.memorydb.db.SimpleDataRepository;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository extends SimpleDataRepository<BookEntity, Long> {
}
