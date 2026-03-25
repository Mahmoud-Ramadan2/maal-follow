package com.mahmoud.maalflow.modules.shared.settings.entity;

import com.mahmoud.maalflow.modules.shared.settings.enums.TemplateType;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing export templates for Excel and other formats.
 */
@Entity
@Table(name = "export_template", indexes = {
    @Index(name = "idx_template_type", columnList = "template_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false)
    private TemplateType templateType;

    @Column(name = "template_config", nullable = false, columnDefinition = "JSON")
    private String templateConfig;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false,
                foreignKey = @ForeignKey(name = "fk_template_created_by"))
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

