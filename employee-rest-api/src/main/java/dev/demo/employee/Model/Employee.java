package dev.demo.employee.Model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public class Employee {
    
    private long employeeId;

    @NotEmpty
    private String firstName;

    private String midddleName;

    @NotEmpty
    private String lastName;

    private String department;

    @Email
    private String emailAddress;

    private String phoneNumber;

    public String geEmployeeId() {
        return String.valueOf(employeeId);
    }

    public String getFirstName() {
        return String.valueOf(firstName);
    }

    public String getMidddleName() {
        return String.valueOf(midddleName);
    }

    public String getLastName() {
        return String.valueOf(lastName);
    }

    public String getDepartment() {
        return String.valueOf(department);
    }

    public String getPhoneNumber() {
        return String.valueOf(phoneNumber);
    }

    public String getEmail() {
        return String.valueOf(emailAddress);
    }
}
