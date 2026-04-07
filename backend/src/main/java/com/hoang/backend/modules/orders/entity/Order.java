package com.hoang.backend.modules.orders.entity;

import com.hoang.backend.modules.users.entity.Address;
import com.hoang.backend.modules.users.entity.AppUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(length = 20)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;

    @Column(name = "shipping_method", nullable = false, length = 20)
    private String shippingMethod = "standard";

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "checkout_url", length = 500)
    private String checkoutUrl;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (date == null) {
            date = LocalDateTime.now();
        }
        if (shippingMethod == null || shippingMethod.isBlank()) {
            shippingMethod = "standard";
        }
        if (status == null || status.isBlank()) {
            status = "pending";
        }
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        if (isPaid == null) {
            isPaid = false;
        }
    }
}
