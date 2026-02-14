package com.bankapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {
        "transactionPinHash",
        "statusReason",
        "user",
        "outgoingTransactions",
        "incomingTransactions"
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_number", unique = true, length = 100,
            insertable = false, updatable = false)
    private String accountNumber;

    @Column(name = "transaction_pin_hash", nullable = false)
    private String transactionPinHash;

    @Column(name = "account_owner_id", nullable = false, insertable = false, updatable = false)
    private Long accountOwnerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "account_balance", nullable = false)
    private BigDecimal accountBalance;

    @Column(name = "bic_swift_code", nullable = false, length = 20)
    private String bicSwiftCode;

    @Column(name = "overdraft_limit",  nullable = false)
    private BigDecimal overdraftLimit;

    @Column(name = "opening_date", nullable = false)
    private LocalDate openingDate;

    @Column(name = "closing_date")
    private LocalDate closingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "status_reason", length = 2000)
    private String statusReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "account_owner_id",
            nullable = false
    )
    private User user;

    @OneToMany(
            mappedBy = "fromAccount",
            fetch = FetchType.LAZY
    )
    private Set<Transaction> outgoingTransactions = new HashSet<>();

    @OneToMany(
            mappedBy = "toAccount",
            fetch = FetchType.LAZY
    )
    private Set<Transaction> incomingTransactions = new HashSet<>();

    public Account(Long accountId, String accountNumber, String transactionPinHash, Long accountOwnerId, AccountType accountType,
                   BigDecimal accountBalance, String bicSwiftCode, BigDecimal overdraftLimit,
                   LocalDate openingDate, LocalDate closingDate, AccountStatus accountStatus,
                   String statusReason) {

        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.transactionPinHash = transactionPinHash;
        this.accountOwnerId = accountOwnerId;
        this.accountType = accountType;
        this.accountBalance = accountBalance;
        this.bicSwiftCode = bicSwiftCode;
        this.overdraftLimit = overdraftLimit;
        this.openingDate = openingDate;
        this.closingDate = closingDate;
        this.accountStatus = accountStatus;

        if (statusReason == null) {
            this.statusReason = "None";
        } else {
            this.statusReason = statusReason;
        }
    }

    public Account(Long accountOwnerId, String transactionPinHash, AccountType accountType,
                   String bicSwiftCode) {

        this.accountOwnerId = accountOwnerId;
        this.transactionPinHash = transactionPinHash;
        this.accountType = accountType;
        this.bicSwiftCode = bicSwiftCode;
    }

    @PrePersist
    public void prePersist() {
        if (statusReason == null) {
            statusReason = "None";
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.accountOwnerId = user.getUserId();
        } else {
            this.accountOwnerId = null;
        }
    }

    public void addOutgoingTransaction(Transaction tx) {
        outgoingTransactions.add(tx);
        tx.setFromAccount(this);
    }

    public void addIncomingTransaction(Transaction tx) {
        incomingTransactions.add(tx);
        tx.setToAccount(this);
    }

    public void removeOutgoingTransaction(Transaction tx) {
        outgoingTransactions.remove(tx);
        tx.setFromAccount(null);
    }

    public void removeIncomingTransaction(Transaction tx) {
        incomingTransactions.remove(tx);
        tx.setToAccount(null);
    }

}
