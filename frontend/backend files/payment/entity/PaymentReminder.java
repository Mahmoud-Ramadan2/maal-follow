package com.mahmoud.maalflow.modules.installments.payment.entity;

import com.mahmoud.maalflow.modules.installments.contract.entity.InstallmentSchedule;
import com.mahmoud.maalflow.modules.installments.payment.enums.ReminderMethod;
import com.mahmoud.maalflow.modules.installments.payment.enums.ReminderStatus;
import com.mahmoud.maalflow.modules.shared.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for tracking payment reminders.
* Implements requirement 17: “Make a reminder to submit the claim five days before it is paid.”
 * Implements requirement 19: “Make a reminder of payment dates to claim on time.”
 * */

@Entity
@Table(name = "payment_reminder")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_schedule_id", nullable = false)
    private InstallmentSchedule installmentSchedule;

    @Column(name = "reminder_date", nullable = false)
    private LocalDate reminderDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "days_before_due", nullable = false)
    private Integer daysBeforeDue;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "reminder_message", columnDefinition = "TEXT")
    private String reminderMessage;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = true; // Continue sending until payment received

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @NotNull
    @ColumnDefault("'PHONE_CALL'")
    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_method")
    private ReminderMethod reminderMethod = ReminderMethod.PHONE_CALL;


    @Override
    public String toString() {
        return "PaymentReminder{" +
                "id=" + id +
                ", reminderDate=" + reminderDate +
                ", dueDate=" + dueDate +
                ", status=" + status +
                '}';
    }
}
