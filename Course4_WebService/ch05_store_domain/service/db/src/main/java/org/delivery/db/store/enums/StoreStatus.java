package org.delivery.db.store.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum StoreStatus {
    REGISTER("등록"),
    UNREGISTER("해지"),
    ;

    private String description;
}
