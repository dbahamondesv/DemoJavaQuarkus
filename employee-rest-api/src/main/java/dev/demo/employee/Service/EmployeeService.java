package dev.demo.employee.Service;

import java.util.List;
import java.util.stream.Collectors;

import dev.demo.employee.Mappers.EmployeeMapper;
import dev.demo.employee.Model.Employee;
import dev.demo.employee.Repository.EmployeeRepository;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationScoped
public class EmployeeService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeService.class);
    
    @Inject
    private final EmployeeRepository employeeRepository;
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
        // It's better to ensure blocking calls run on a worker thread
        return Uni.createFrom().item(() -> employeeRepository.listAll())
                .map(entities -> entities.stream()
                .map(employeeMapper::toDomain)
                .collect(Collectors.toList()))
                .invoke(list -> LOGGER.info("Service.findAll() - found {} employees", list.size()))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onFailure().invoke(f -> LOGGER.error("Service.findAll() - failed to find all employees", f));
        
    }

    public Uni<Employee> findById(long employeeId) {
        LOGGER.debug("Service: findById({}) - start", employeeId);
        
        return Uni.createFrom().item(() -> employeeRepository.findByIdOptional(employeeId))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .map(optionalEntity -> 
                    optionalEntity.orElseThrow(() -> 
                        new NotFoundException("Employee not found with id: " + employeeId)))
                .map(entity -> employeeMapper.toDomain(entity))
                .invoke(employee -> LOGGER.info("Service: findById({}) - employee found", employeeId))
                .onFailure().invoke(f -> LOGGER.error("Service: findById({}) - failed to find employee", employeeId, f));

    }

    
    public Uni<Employee> save(Employee employee)
    {
        LOGGER.debug("Service: save() - init");
        var entity = employeeMapper.toEntity(employee);

        return Uni.createFrom().item(()->{
            // persist is a blocking operation
            employeeRepository.persist(entity);
            return employeeMapper.toDomain(entity);
        })
        .invoke(saved -> LOGGER.info("Service: save() - saved employee successfully"))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .onFailure().invoke(f -> LOGGER.error("Service: save() - failed to save employee", f));
    }

    @Transactional
    public Uni<Employee> update(long employeeId, Employee employee) {
        return Uni.createFrom().item(() -> employeeRepository.findByIdOptional(employeeId))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onItem().ifNotNull().transformToUni(entity -> {
                    var updatedEntity = employeeMapper.toEntity(employee);
                    updatedEntity.setEmployeeId(employeeId); // Make sure the ID doesn't change
                    // Panache's persist() handles the update if the entity already exists
                    employeeRepository.persist(updatedEntity);
                    return Uni.createFrom().item(employeeMapper.toDomain(updatedEntity));
                })
                .onItem().ifNull().failWith(() -> new NotFoundException("Employee not found with id: " + employeeId))
                .invoke(e -> LOGGER.info("Service: update({}) - employee updated successfully", employeeId))
                .onFailure().invoke(f -> LOGGER.error("Service: update({}) - update failed", employeeId, f));
    }

    @Transactional
    public Uni<Boolean> deleteById(Long id) {
        return Uni.createFrom().item(() -> employeeRepository.deleteById(id))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
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
