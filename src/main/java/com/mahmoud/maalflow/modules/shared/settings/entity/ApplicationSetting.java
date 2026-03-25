package com.mahmoud.maalflow.modules.shared.settings.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ApplicationSetting entity for system configuration.
 *
 * @author Mahmoud
 */
@Entity
@Table(name = "application_setting", indexes = {
        @Index(name = "idx_setting_key", columnList = "setting_key", unique = true),
        @Index(name = "idx_setting_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    @Column(name = "description")
    private String description;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "is_encrypted")
    private Boolean isEncrypted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "ApplicationSetting{" +
                "id=" + id +
                ", settingKey='" + settingKey + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}

