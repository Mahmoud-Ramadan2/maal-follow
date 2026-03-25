package com.mahmoud.maalflow.modules.installments.purchase.entity;

import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.vendor.entity.Vendor;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_purchase")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "product_name")
    private String productName;
    @Column(name = "buy_price", precision = 12, scale = 2)
    private BigDecimal buyPrice;
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(name = "created_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    @ToString.Exclude
    private Vendor vendor;
    @OneToMany(mappedBy = "purchase", fetch = FetchType.LAZY)
    private List<Contract> contracts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @ToString.Exclude
    private User createdBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


}
