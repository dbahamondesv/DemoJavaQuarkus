package dev.demo.employee;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import dev.demo.employee.Mappers.EmployeeMapper;
import dev.demo.employee.Model.Employee;
import dev.demo.employee.Repository.EmployeeRepository;
import dev.demo.employee.Service.EmployeeService;
import dev.demo.employee.Entity.EmployeeEntity;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.NotFoundException;

@DisplayName("EmployeeService Unit Tests")
public class EmployeeServiceTest {

    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    EmployeeService employeeService;

    @Mock
    EmployeeEntity employeeEntity;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("findAll() should return a list of employees")
    void findAll_shouldReturnListOfEmployees() {
        // Arrange
        Employee employee1 = new Employee();
        employee1.setEmployeeId(1L);
        employee1.setFirstName("John");
        employee1.setLastName("Doe");

        Employee employee2 = new Employee();
        employee2.setEmployeeId(2L);
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");

        List<Employee> employeeList = Arrays.asList(employee1, employee2);

        when(employeeRepository.listAll()).thenReturn(Uni.createFrom().item(employeeList));
        when(employeeMapper.toDomain(employee1)).thenReturn(employee1);
         when(employeeMapper.toDomain(employee2)).thenReturn(employee2);

        // Act
        List<Employee> result = employeeService.findAll().await().indefinitely();

        // Assert
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("John Doe", result.get(0).getFirstName()+ " " + result.get(0).getLastName());
        Assertions.assertEquals("Jane Smith", result.get(1).getFirstName()+ " " + result.get(1).getLastName());
    }

    @Test
    @DisplayName("findById() should return an employee when found")
    void findById_shouldReturnEmployee() {
        // Arrange
        long employeeId = 1L;
        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setFirstName("John");
        employee.setLastName("Doe");


        when(employeeRepository.findById(employeeId)).thenReturn(Uni.createFrom().item(employee));
        when(employeeMapper.toDomain(employee)).thenReturn(employee);

        // Act
        Employee result = employeeService.findById(employeeId).await().indefinitely();

        // Assert
        Assertions.assertEquals(employeeId, result.getEmployeeId());
        Assertions.assertEquals("John Doe", result.getFirstName()+ " " + result.getLastName());
    }

    @Test
    @DisplayName("findById() should throw NotFoundException when employee not found")
    void findById_shouldReturnNotFoundException() {
        // Arrange
        long employeeId = 1L;
        when(employeeRepository.findById(employeeId)).thenReturn(Uni.createFrom().nullItem());

        // Act & Assert
        Assertions.assertThrows(NotFoundException.class, () -> {
            employeeService.findById(employeeId).await().indefinitely();
        });
    }

    @Test
    @DisplayName("save() should persist and return an employee")
    void save_shouldReturnSavedEmployee() {
        // Arrange
        Employee employeeToSave = new Employee();
        employeeToSave.setFirstName("New");
        employeeToSave.setLastName("Employee");

        Employee savedEmployee = new Employee();
        savedEmployee.setEmployeeId(1L);
        savedEmployee.setFirstName("New");
        savedEmployee.setLastName("Employee");
        

        // Mocking entity conversion and persistence
        when(employeeMapper.toEntity(any(Employee.class))).thenReturn(new EmployeeEntity());
        when(employeeRepository.persistAndFlush(any())).thenReturn(Uni.createFrom().item(new EmployeeEntity()));
        when(employeeMapper.toDomain(any(EmployeeEntity.class))).thenReturn(savedEmployee);

        // Act
        Employee result = employeeService.save(employeeToSave).await().indefinitely();

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(savedEmployee.getEmployeeId(), result.getEmployeeId());
        Assertions.assertEquals(savedEmployee.getFirstName(), result.getFirstName());
        Assertions.assertEquals(savedEmployee.getLastName(), result.getLastName());
        verify(employeeRepository).persistAndFlush(any());
    }

    @Nested
    @DisplayName("Update scenarios")
    class UpdateTests {

        @Test
        @DisplayName("update() should update and return employee when found")
        void update_shouldReturnUpdatedEmployee() {
            // Arrange
            long employeeId = 1L;
            Employee employeeToUpdate = new Employee();
            employeeToUpdate.setFirstName("Updated");

            Employee existingEntity = new Employee();
            existingEntity.setEmployeeId(employeeId);
            existingEntity.setFirstName("Original");

            when(employeeRepository.findById(employeeId)).thenReturn(Uni.createFrom().item(existingEntity));
            when(employeeRepository.persistAndFlush(any())).thenReturn(Uni.createFrom().item(existingEntity));
            when(employeeMapper.toDomain(existingEntity)).thenAnswer(invocation -> {
                Employee updatedDomain = new Employee();
                updatedDomain.setEmployeeId(employeeId);
                updatedDomain.setFirstName("Updated");
                updatedDomain.setLastName("Name");
                return updatedDomain;
            });

            // Act
            Employee result = employeeService.update(employeeId, employeeToUpdate).await().indefinitely();

            // Assert
            Assertions.assertNotNull(result);
            Assertions.assertEquals(employeeId, result.getEmployeeId());
            Assertions.assertEquals("Updated", result.getFirstName());
            Assertions.assertEquals("Name", result.getLastName());
            verify(employeeMapper).updateEntityFromDomain(employeeToUpdate, existingEntity);
            verify(employeeRepository).persistAndFlush(existingEntity);
        }

        @Test
        @DisplayName("update() should throw NotFoundException when employee not found")
        void update_shouldThrowNotFoundException() {
            // Arrange
            long employeeId = 1L;
            Employee employeeToUpdate = new Employee();
            when(employeeRepository.findById(employeeId)).thenReturn(Uni.createFrom().nullItem());

            // Act & Assert
            Assertions.assertThrows(NotFoundException.class, () -> {
                employeeService.update(employeeId, employeeToUpdate).await().indefinitely();
            });
            verify(employeeRepository, never()).persistAndFlush(any());
        }
    }

    @Test
    @DisplayName("deleteById() should return true when employee is deleted")
    void deleteById_shouldReturnTrue() {
        // Arrange
        long employeeId = 1L;
        when(employeeRepository.deleteById(employeeId)).thenReturn(Uni.createFrom().item(true));

        // Act
        boolean result = employeeService.deleteById(employeeId).await().indefinitely();

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("deleteById() should return false when employee not found")
    void deleteById_shouldReturnFalse() {
        // Arrange
        long employeeId = 1L;
        when(employeeRepository.deleteById(anyLong())).thenReturn(Uni.createFrom().item(false));

        // Act
        boolean result = employeeService.deleteById(employeeId).await().indefinitely();

        // Assert
        Assertions.assertFalse(result);
    }
}
