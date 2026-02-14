package com.bankapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "employmentprofile")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user"})
public class EmploymentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "employment_profile_id")
    private Long employmentProfileId;

    @Column(name = "user_id", nullable = false, insertable = false, updatable = false)
    private Long userId;

    @Column(name = "occupation", nullable = false)
    private String occupation;

    @Column(name = "annual_income",  nullable = false)
    private BigDecimal annualIncome;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_of_funds",  nullable = false)
    private SourceOfFunds sourceOfFunds;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_purpose", nullable = false)
    private AccountPurpose accountPurpose;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private User user;

    public EmploymentProfile(Long employmentProfileId, Long userId, String occupation,
                             BigDecimal annualIncome, SourceOfFunds sourceOfFunds,
                             AccountPurpose accountPurpose) {

        this.employmentProfileId = employmentProfileId;
        this.userId = userId;
        this.occupation = occupation;
        this.annualIncome = annualIncome;
        this.sourceOfFunds = sourceOfFunds;
        this.accountPurpose = accountPurpose;
    }

    public EmploymentProfile(Long employmentProfileId, String occupation, BigDecimal annualIncome,
                             SourceOfFunds sourceOfFunds, AccountPurpose accountPurpose) {

        this.employmentProfileId = employmentProfileId;
        this.occupation = occupation;
        this.annualIncome = annualIncome;
        this.sourceOfFunds = sourceOfFunds;
        this.accountPurpose = accountPurpose;
    }

    public EmploymentProfile(String occupation, BigDecimal annualIncome,
                             SourceOfFunds sourceOfFunds, AccountPurpose accountPurpose) {

        this.occupation = occupation;
        this.annualIncome = annualIncome;
        this.sourceOfFunds = sourceOfFunds;
        this.accountPurpose = accountPurpose;
    }
}
