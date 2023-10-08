package org.delivery.db.userordermenu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.delivery.db.BaseEntity;
import org.delivery.db.store.StoreEntity;
import org.delivery.db.storemenu.StoreMenuEntity;
import org.delivery.db.userorder.UserOrderEntity;
import org.delivery.db.userordermenu.enums.UserOrderMenuStatus;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "user_order_menu")
public class UserOrderMenuEntity extends BaseEntity {
    @JoinColumn(nullable = false)
    @ManyToOne
    private UserOrderEntity userOrder;
    @JoinColumn(nullable = false)
    @ManyToOne
    private StoreMenuEntity storeMenu;
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private UserOrderMenuStatus status;

    public UserOrderEntity getUserOrder() {
        return userOrder;
    }

    public void setUserOrder(UserOrderEntity userOrder) {
        this.userOrder = userOrder;
    }

    public StoreMenuEntity getStoreMenu() {
        return storeMenu;
    }

    public void setStoreMenu(StoreMenuEntity storeMenu) {
        this.storeMenu = storeMenu;
    }

    public UserOrderMenuStatus getStatus() {
        return status;
    }

    public void setStatus(UserOrderMenuStatus status) {
        this.status = status;
    }
}
