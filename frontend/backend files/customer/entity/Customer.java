package com.mahmoud.maalflow.modules.installments.customer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mahmoud.maalflow.modules.installments.contract.entity.Contract;
import com.mahmoud.maalflow.modules.installments.schedule.entity.CollectionRouteItem;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Customer entity for installment contracts.
 *
 * @author Mahmoud
 */
@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{validation.name.required}")
    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(name = "national_id", length = 50)
    private String nationalId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private List<Contract> contracts;

    @OneToMany(mappedBy = "linkedCustomer")
    @JsonIgnore
    private Set<CustomerAccountLink> accountLinks = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    @JsonIgnore
    private Set<CustomerAccountLink> linkedBy = new LinkedHashSet<>();

    @OneToMany(mappedBy = "customer")
    private Set<CollectionRouteItem> collectionRouteItems = new LinkedHashSet<>();

    /**
     * Constructor for creating customer with basic info.
     */
    public Customer(String name, String phone, String address, String nationalId, String notes) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.nationalId = nationalId;
        this.notes = notes;
    }

    public void addAccountLink(CustomerAccountLink link) {
        accountLinks.add(link);
        link.setCustomer(this);
    }

    public void removeAccountLink(CustomerAccountLink link) {
        accountLinks.remove(link);
        link.setCustomer(null);
    }
    public void  addLinkedBy(CustomerAccountLink link) {
        linkedBy.add(link);
        link.setLinkedCustomer(this);
    }
    public void removeLinkedBy(CustomerAccountLink link) {
        linkedBy.remove(link);
        link.setLinkedCustomer(null);
    }


    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", nationalId='" + nationalId + '\'' +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", active=" + active +
                '}';
    }
}
