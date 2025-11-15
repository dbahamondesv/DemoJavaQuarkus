package dev.demo.employee.Service;

import java.util.List;
import java.util.stream.Collectors; 
import dev.demo.employee.Mappers.EmployeeMapper; // This line is kept as it is used in the code
import dev.demo.employee.Model.Employee;
import dev.demo.employee.Repository.EmployeeRepository;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class EmployeeService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeService.class);
    
    private final EmployeeRepository employeeRepository; // @Inject removed, constructor injection is used
    private final EmployeeMapper employeeMapper;

    //Constructor Injection
    @Inject
    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;  
    }
    @WithTransaction
    public Uni<List<Employee>> findAll() {
    LOGGER.debug("Service.findAll() - init");
    return Uni.createFrom().item(employeeRepository.listAll())
        .map(entities -> entities.stream()
            .map(employeeMapper::toDomain)
            .collect(Collectors.toList()))
        .invoke(list -> LOGGER.info("Service.findAll() - found {} employees", list.size()))
        .onFailure().invoke(f -> LOGGER.error("Service.findAll() - failed to find all employees", f));
    }
    @WithTransaction
    public Uni<Employee> findById(long employeeId) {
        LOGGER.debug("Service: findById({}) - start", employeeId);
        // Use the reactive `findById` which returns Uni<Entity>
        return Uni.createFrom().item(employeeRepository.findById(employeeId))
            .onItem().ifNull().failWith(() -> new NotFoundException("Employee not found with id: " + employeeId))
            .onItem().ifNotNull().transform(employeeMapper::toDomain)
            .invoke(employee -> LOGGER.info("Service: findById({}) - employee found", employeeId))
            .onFailure().invoke(f -> LOGGER.error("Service: findById({}) - failed to find employee", employeeId, f));
    }

    @WithTransaction
public Uni<Void> save(Employee employee) {
    LOGGER.debug("Service: save() - init");
    var entity = employeeMapper.toEntity(employee);
    employeeRepository.persistAndFlush(entity); // ejecuta y no retorna nada
    return Uni.createFrom().voidItem()
        .invoke(() -> LOGGER.info("Service: save() - saved employee successfully"));
}

  @WithTransaction
public Uni<Employee> update(long employeeId, Employee employee) {
    return employeeRepository.findById(employeeId)
        .onItem().ifNotNull().transformToUni(entity -> {
            employeeMapper.updateEntityFromDomain(employee, entity);
            employeeRepository.persistAndFlush(entity); // ejecuta pero no retorna nada
            return Uni.createFrom().item(employeeMapper.toDomain(entity));
        })
        .onItem().ifNull().failWith(() -> new NotFoundException("Employee not found with id: " + employeeId))
        .invoke(e -> LOGGER.info("Service: update({}) - employee updated successfully", employeeId))
        .onFailure().invoke(f -> LOGGER.error("Service: update({}) - update failed", employeeId, f));
}

    @WithTransaction
    public Uni<Boolean> deleteById(Long id) {
        // Use the reactive `deleteById` which returns Uni<Boolean>
        return employeeRepository.deleteById(id).invoke(deleted -> {
            if (deleted) {
                LOGGER.info("Service: deleteById({}) - employee deleted successfully", id);
            } else {
                LOGGER.warn("Service: deleteById({}) - employee not found", id);
            }
        })
                .onFailure().invoke(f -> LOGGER.error("Service: deleteById({}) - failed to delete employee", id, f));
    }

}