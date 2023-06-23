package com.example.simpleboard.crud;

public interface Converter<DTO, ENTITY> {
    DTO toDTO(ENTITY entity);

    ENTITY toEntity(DTO dto);
}
