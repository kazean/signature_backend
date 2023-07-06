package org.delivery.db.store.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum StoreStatus {
    REGISTER("등록"),
    UNREGISTERED("해지")
    ;
    private String description;
}
