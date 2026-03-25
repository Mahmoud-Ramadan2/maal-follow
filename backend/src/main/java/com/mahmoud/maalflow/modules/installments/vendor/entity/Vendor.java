package com.mahmoud.maalflow.modules.installments.vendor.entity;

import com.mahmoud.maalflow.modules.installments.purchase.entity.Purchase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendor")
@Data
@NoArgsConstructor
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(nullable = false)
    private String name;
    private String phone;
    private String address;
    @Column(columnDefinition = "Text")
    private String notes;
    @Column(name = "created_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
    private Boolean active = true;

    @OneToMany(mappedBy = "vendor", fetch = FetchType.LAZY)
    private List<Purchase> purchases;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    public void addPurchases(Purchase purchase) {
        if(purchases == null){
            purchases = new ArrayList<>();
        }
        purchases.add(purchase);
        purchase.setVendor(this);
    }

}