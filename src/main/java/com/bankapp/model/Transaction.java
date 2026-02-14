package com.bankapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"fromAccount", "toAccount"})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "from_account_id")
    private Long fromAccountId;

    @Column(name = "to_account_id")
    private Long toAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "available_balance_after",  nullable = false)
    private BigDecimal availableBalanceAfter;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false)
    private TransactionStatus transactionStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", insertable = false, updatable = false)
    private Account fromAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", insertable = false, updatable = false)
    private Account toAccount;

    public Transaction(Long transactionId, Long fromAccountId, Long toAccountId,
                       TransactionType transactionType, BigDecimal amount,
                       BigDecimal availableBalanceAfter, LocalDateTime transactionDate,
                       TransactionStatus transactionStatus) {

        this.transactionId = transactionId;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.availableBalanceAfter = availableBalanceAfter;
        this.transactionDate = transactionDate;
        this.transactionStatus = transactionStatus;
    }

    public Transaction(Long fromAccountId, Long toAccountId,
                       TransactionType transactionType, BigDecimal amount,
                       BigDecimal availableBalanceAfter,
                       TransactionStatus transactionStatus) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.availableBalanceAfter = availableBalanceAfter;
        this.transactionStatus = transactionStatus;
    }

    public void setFromAccount(Account account) {
        this.fromAccount = account;
        this.fromAccountId = (account != null) ? account.getAccountId() : null;
    }

    public void setToAccount(Account account) {
        this.toAccount = account;
        this.toAccountId = (account != null) ? account.getAccountId() : null;
    }


    @PrePersist
    public void prePersist() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
}
