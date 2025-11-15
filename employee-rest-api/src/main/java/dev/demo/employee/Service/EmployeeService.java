import java.util.List;
import java.util.stream.Collectors; 
import dev.demo.employee.Mappers.EmployeeMapper; // This line is kept as it is used in the code
import dev.demo.employee.Model.Employee; 
import io.quarkus.hibernate.reactive.panache.common.With  ReactiveTransactional;
import dev.demo.employee.Repository.EmployeeRepository;
import io.smallrye.mutiny.infrastructure.Infrastructure;
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

    public Uni<List<Employee>> findAll()
    {
        LOGGER.debug("Service.findAll() - init");
        // Use the reactive `listAll` which returns Uni<List<Entity>> directly
        return employeeRepository.listAll() // This returns Uni<List<EmployeeEntity>>
                .onItem().transform(entities -> entities.stream() // entities is List<EmployeeEntity>
                .map(employeeMapper::toDomain) // map each EmployeeEntity to Employee
                .collect(Collectors.toList()))
                .invoke(list -> LOGGER.info("Service.findAll() - found {} employees", list.size()))
                .onFailure().invoke(f -> LOGGER.error("Service.findAll() - failed to find all employees", f));
    }

    public Uni<Employee> findById(long employeeId) {
        LOGGER.debug("Service: findById({}) - start", employeeId);
        // Use the reactive `findById` which returns Uni<Entity>
        return employeeRepository.findById(employeeId)
                .onItem().ifNull().failWith(() -> new NotFoundException("Employee not found with id: " + employeeId))
                .onItem().ifNotNull().transform(employeeMapper::toDomain)
                .invoke(employee -> LOGGER.info("Service: findById({}) - employee found", employeeId))
                .onFailure().invoke(f -> LOGGER.error("Service: findById({}) - failed to find employee", employeeId, f));
    }

    @ReactiveTransactional
    public Uni<Employee> save(Employee employee)
    {
        LOGGER.debug("Service: save() - init");
        var entity = employeeMapper.toEntity(employee);
        // Use persistAndFlush to get the persisted entity back with its ID
        return employeeRepository.persistAndFlush(entity)
        .onItem().transform(persistedEntity -> employeeMapper.toDomain(persistedEntity))
        .invoke(saved -> LOGGER.info("Service: save() - saved employee successfully with id {}", saved.getEmployeeId()))
        .onFailure().invoke(f -> LOGGER.error("Service: save() - failed to save employee", f));
    }

    @ReactiveTransactional
    public Uni<Employee> update(long employeeId, Employee employee) {
        return employeeRepository.findById(employeeId)
                .onItem().ifNotNull().transformToUni(entity -> { // entity is the object from the DB
                    employeeMapper.updateEntityFromDomain(employee, entity); // Update the DB entity with the new data
                    return employeeRepository.persistAndFlush(entity).map(persistedEntity -> employeeMapper.toDomain(persistedEntity));
                })
                .onItem().ifNull().failWith(() -> new NotFoundException("Employee not found with id: " + employeeId))
                .invoke(e -> LOGGER.info("Service: update({}) - employee updated successfully", employeeId))
                .onFailure().invoke(f -> LOGGER.error("Service: update({}) - update failed", employeeId, f));
    }

    @ReactiveTransactional
    public Uni<Boolean> deleteById(Long id) {
        // Use the reactive `deleteById` which returns Uni<Boolean>
        return employeeRepository.deleteById(id)
                .invoke(deleted -> {
                    if (deleted) {
                        LOGGER.info("Service: deleteById({}) - employee deleted successfully", id);
                    } else {
                        LOGGER.warn("Service: deleteById({}) - employee not found", id);
                    }
                })
                .onFailure().invoke(f -> LOGGER.error("Service: deleteById({}) - failed to delete employee", id, f));
    }

}