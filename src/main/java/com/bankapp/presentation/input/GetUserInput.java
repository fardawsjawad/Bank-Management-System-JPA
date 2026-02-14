package com.bankapp.presentation.input;

import com.bankapp.model.*;
import com.bankapp.security.PasswordHasher;
import com.bankapp.validation.EmploymentProfileValidator;
import com.bankapp.validation.UserAddressValidator;
import com.bankapp.validation.UserValidator;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GetUserInput {

    private GetUserInput() {  }

    public static User getUser(boolean getUserForUpdate) {

        String password = "";
        if (!getUserForUpdate) {
            System.out.print("\nEnter password: ");
            password = ConsoleReader.readString();
        }

        System.out.print("Enter first name: ");
        String firstName = ConsoleReader.readString();
        System.out.print("Enter last name: ");
        String lastName = ConsoleReader.readString();
        System.out.print("Enter date-of-birth (yyyy-mm-dd): ");
        LocalDate birthDate = ConsoleReader.readLocalDate();
        System.out.print("Enter gender (Male, Female, Other): ");
        Gender gender = ConsoleReader.readGender();

        System.out.print("Enter email address: ");
        String email = ConsoleReader.readString();
        if (!UserValidator.isValidEmail(email)) {
            System.out.println("Invalid email address");
            return null;
        }

        System.out.print("Enter nationality: ");
        String nationality = ConsoleReader.readString();
        System.out.print("Enter passport number: ");
        String passport = ConsoleReader.readString();
        System.out.print("Enter phone number: ");
        String phoneNumber = ConsoleReader.readString();


        User user = new User(
                firstName,
                lastName,
                birthDate,
                gender,
                email,
                nationality,
                passport,
                phoneNumber
        );

        if (!getUserForUpdate) {
            user.setPasswordHash(password);
        }

        return user;
    }

    public static EmploymentProfile getEmploymentProfile() {

        System.out.print("\nEnter your occupation: ");
        String occupation = ConsoleReader.readString();
        if (!EmploymentProfileValidator.isValidOccupation(occupation)) {
            System.out.println("Invalid occupation");
            return null;
        }

        System.out.print("\nWhat is your annual income: ");
        String annualIncomeStr = ConsoleReader.readString();
        if (!EmploymentProfileValidator.isValidAnnualIncome(annualIncomeStr)) {
            System.out.println("Invalid annual income");
            return null;
        }

        BigDecimal annualIncome = null;
        try {
            annualIncome = BigDecimal.valueOf(Double.parseDouble(annualIncomeStr));
        } catch (NumberFormatException e) {
            System.out.println("Invalid annual income");
            return null;
        }

        SourceOfFunds sourceOfFunds = null;
        AccountPurpose accountPurpose = null;

        System.out.print("Choose source of funds: ");
        System.out.println("\n1. Salary");
        System.out.println("2. Business");
        System.out.println("3. Investment");
        System.out.println("4. Other");
        System.out.print("Enter your choice: ");
        int sourceChoice = ConsoleReader.readInt();

        switch (sourceChoice) {
            case 1 -> sourceOfFunds = SourceOfFunds.SALARY;
            case 2 -> sourceOfFunds = SourceOfFunds.BUSINESS;
            case 3 ->  sourceOfFunds = SourceOfFunds.INVESTMENT;
            case 4 ->  sourceOfFunds = SourceOfFunds.OTHER;
            default -> {
                System.out.println("Invalid choice");
                return null;
            }
        }

        System.out.print("What is the purpose of your account: ");
        System.out.println("\n1. Personal");
        System.out.println("2. Business");
        System.out.println("3. Savings");
        System.out.println("4. Other");
        System.out.print("Enter your choice: ");
        int purposeChoice = ConsoleReader.readInt();

        switch (purposeChoice) {
            case 1 -> accountPurpose = AccountPurpose.PERSONAL;
            case 2 -> accountPurpose = AccountPurpose.BUSINESS;
            case 3 -> accountPurpose = AccountPurpose.SAVINGS;
            case 4 -> accountPurpose = AccountPurpose.OTHER;
            default -> {
                System.out.println("Invalid choice");
                return null;
            }
        }

        return new EmploymentProfile(
                occupation,
                annualIncome,
                sourceOfFunds,
                accountPurpose
        );
    }

    public static UserAddress getUserAddress() {

        System.out.println("Choose address type: ");
        System.out.println("1. Residential");
        System.out.println("2. Mailing");
        System.out.println("3. Office");
        System.out.println("4. Permanent");
        System.out.println("5. Temporary");
        System.out.print("Enter your choice: ");

        int addressTypeChoice = ConsoleReader.readInt();

        AddressType addressType = null;

        switch (addressTypeChoice) {
            case 1:
                addressType = AddressType.RESIDENTIAL;
                break;
            case 2:
                addressType = AddressType.MAILING;
                break;
            case 3:
                addressType = AddressType.OFFICE;
                break;
            case 4:
                addressType = AddressType.PERMANENT;
                break;
            case 5:
                addressType = AddressType.TEMPORARY;
                break;
            default: {
                System.out.println("Invalid choice");
                return null;
            }
        }

        System.out.print("Address Line 1: ");
        String addressLine1 = ConsoleReader.readString();
        if (!UserAddressValidator.isValidAddressLine1(addressLine1)) {
            System.out.println("Invalid address line 1");
            return null;
        }

        System.out.print("Address Line 2: ");
        String addressLine2 = ConsoleReader.readString();
        if  (!UserAddressValidator.isValidAddressLine2(addressLine2)) {
            System.out.println("Invalid address line 2");
            return null;
        }

        System.out.print("Locality: ");
        String locality = ConsoleReader.readString();
        if (!UserAddressValidator.isValidLocality(locality)) {
            System.out.println("Invalid locality");
            return null;
        }

        System.out.print("City: ");
        String city = ConsoleReader.readString();
        if  (!UserAddressValidator.isValidCity(city)) {
            System.out.println("Invalid city");
            return null;
        }

        System.out.println("District (optional): ");
        String district = ConsoleReader.readString();
        if (!UserAddressValidator.isValidDistrict(district)) {
            System.out.println("Invalid district");
            return null;
        }

        System.out.print("State or Province: ");
        String stateOrProvince = ConsoleReader.readString();
        if (!UserAddressValidator.isValidStateOrProvince(stateOrProvince)) {
            System.out.println("Invalid state or province");
            return null;
        }

        System.out.print("Postal code: ");
        String postalCode = ConsoleReader.readString();
        if (!UserAddressValidator.isValidPostalCode(postalCode)) {
            System.out.println("Invalid postal code");
            return null;
        }

        System.out.print("Country: ");
        String country = ConsoleReader.readString();
        if (!UserAddressValidator.isValidCountry(country)) {
            System.out.println("Invalid country");
            return null;
        }

        if (stateOrProvince.isBlank()) stateOrProvince = null;

        return new UserAddress(
                addressType,
                addressLine1,
                addressLine2,
                locality,
                city,
                district,
                stateOrProvince,
                postalCode,
                country
        );
    }

    public static Account generateAccountForCreation() {
        System.out.println("Choose account type: ");
        System.out.println("1. Savings");
        System.out.println("2. Current");
        System.out.print("Enter your choice: ");
        int accountTypeChoice = ConsoleReader.readInt();

        AccountType accountType = null;

        switch (accountTypeChoice) {
            case 1 -> accountType = AccountType.SAVINGS;
            case 2 -> accountType = AccountType.CURRENT;
            default -> {
                System.out.println("Invalid choice");
                return null;
            }
        }

        System.out.print("Enter Account PIN code: ");
        String  accountPinCode = ConsoleReader.readString();

        Account account = new Account();
        account.setTransactionPinHash(accountPinCode);
        account.setAccountType(accountType);
        account.setBicSwiftCode("KKBK0008");

        return account;
    }

    public static TransactionStatus getTransactionStatus() {
        System.out.println("\nSelect the status to fetch transactions by:");
        System.out.println("1. Pending");
        System.out.println("2. Completed");
        System.out.println("3. Failed");
        System.out.println("4. Cancelled");
        System.out.println("5. Reversed");
        System.out.print("Enter your choice: ");
        int choice =  ConsoleReader.readInt();

        TransactionStatus transactionStatus = null;
        switch (choice) {
            case 1 -> transactionStatus = TransactionStatus.PENDING;
            case 2 -> transactionStatus = TransactionStatus.COMPLETED;
            case 3 -> transactionStatus = TransactionStatus.FAILED;
            case 4 -> transactionStatus = TransactionStatus.CANCELLED;
            case 5 -> transactionStatus = TransactionStatus.REVERSED;
            default -> {
                System.out.println("Invalid choice");
                return null;
            }
        }

        return transactionStatus;
    }

}
