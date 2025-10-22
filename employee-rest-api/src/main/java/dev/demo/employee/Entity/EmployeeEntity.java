package dev.demo.employee.Entity;

import io.quarkus.Generated;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;


/**
 * Example JPA entity defined as a Panache Entity.
 * An ID field of Long type is provided, if you want to define your own ID field extends <code>PanacheEntityBase</code> instead.
 *
 * This uses the active record pattern, you can also use the repository pattern instead:
 * .
 *
 * Usage (more example on the documentation)
 *
 * {@code
 *     public void doSomething() {
 *         MyEntity entity1 = new MyEntity();
 *         entity1.field = "field-1";
 *         entity1.persist();
 *
 *         List<MyEntity> entities = MyEntity.listAll();
 *     }
 * }
 */
@Entity(name= "Employee")
@Table(name= "employee")
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name="employee_id")
    private long employeeId;

    @NotEmpty
    @Column(name="first_name")
    private String firstName;

    @Column(name="middle_name")
    private String middleName;

    @NotEmpty
    @Column(name="last_name")
    private String lastName;

    @Column(name="department")
    private String department;

    @Email
    @Column(name="email_address")
    private String emailAddress;

    @Column(name="phone_number")
    private String phoneNumber;

    public String geEmployeeId() {
        return String.valueOf(employeeId);
    }

    public String setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
        return String.valueOf(employeeId);
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setMidddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmailAddress(String email) {
        this.emailAddress = email;
    }
}
