package dev.demo.employee;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import dev.demo.employee.Service.EmployeeService;
import dev.demo.employee.Repository.EmployeeRepository;
import dev.demo.employee.Entity.EmployeeEntity;
import dev.demo.employee.Mappers.EmployeeMapper;
import dev.demo.employee.Model.Employee;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class EmployeeServiceTest {

    private EmployeeRepository repository;
    private EmployeeMapper mapper;
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        repository = mock(EmployeeRepository.class);
        mapper = mock(EmployeeMapper.class);
        service = new EmployeeService(repository, mapper);
    }

    @Test
void testFindAllReturnsEmployees() {
    // Simulamos entidad JPA
    var entity = new EmployeeEntity();
    var domain = new Employee();

    when(repository.listAll()).thenReturn(Uni.createFrom().item(List.of(entity)));
    when(mapper.toDomain(entity)).thenReturn(domain);

    List<Employee> result = service.findAll().await().indefinitely();

    assertEquals(1, result.size());
    assertEquals(domain, result.get(0));
    verify(repository).listAll();
    verify(mapper).toDomain(entity);
}


    @Test
void testFindByIdFound_1() {
    // Simulamos entidad JPA
    EmployeeEntity entity = new EmployeeEntity();
    Employee domain = new Employee();

    when(repository.findById(1L)).thenReturn(Uni.createFrom().item(entity));
    when(mapper.toDomain(entity)).thenReturn(domain);

    Employee result = service.findById(1L).await().indefinitely();

    assertNotNull(result);
    assertEquals(domain, result);
    verify(repository).findById(1L);
    verify(mapper).toDomain(entity);
}


    @Test
    void testFindByIdNotFound() {
        when(repository.findById(1L)).thenReturn(Uni.createFrom().nullItem());

        assertThrows(NotFoundException.class,
            () -> service.findById(1L).await().indefinitely());
    }

  @Test
void testFindByIdFound() {
    // Simulamos entidad JPA
    EmployeeEntity entity = new EmployeeEntity();
    Employee domain = new Employee();

    // El repositorio devuelve la entidad
    when(repository.findById(1L)).thenReturn(Uni.createFrom().item(entity));

    // El mapper convierte la entidad a dominio
    when(mapper.toDomain(entity)).thenReturn(domain);

    // El servicio expone el dominio
    Employee result = service.findById(1L).await().indefinitely();

    assertNotNull(result);
    assertEquals(domain, result);
    verify(repository).findById(1L);
    verify(mapper).toDomain(entity);
}


    @Test
    void testUpdateNotFound() {
        var employee = new Employee();
        when(repository.findById(1L)).thenReturn(Uni.createFrom().nullItem());

        assertThrows(NotFoundException.class,
            () -> service.update(1L, employee).await().indefinitely());
    }

    @Test
    void testDeleteByIdTrue() {
        when(repository.deleteById(1L)).thenReturn(Uni.createFrom().item(true));

        Boolean deleted = service.deleteById(1L).await().indefinitely();

        assertTrue(deleted);
        verify(repository).deleteById(1L);
    }

    @Test
    void testDeleteByIdFalse() {
        when(repository.deleteById(1L)).thenReturn(Uni.createFrom().item(false));

        Boolean deleted = service.deleteById(1L).await().indefinitely();

        assertFalse(deleted);
    }
}
