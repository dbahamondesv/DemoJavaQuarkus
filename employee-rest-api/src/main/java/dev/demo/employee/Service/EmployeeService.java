package dev.demo.employee.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dev.demo.employee.Entity.EmployeeEntity;
import dev.demo.employee.Mappers.EmployeeMapper;
import dev.demo.employee.Model.Employee;
import dev.demo.employee.Repository.EmployeeRepository;
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
    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;  
    }

    public Uni<List<Employee>> findAll()
    {
        LOGGER.debug("Service.findAll() - init");

        return Uni.createFrom().item(() -> employeeRepository.findAll())
                .map(entities -> entities.stream()
                .map(employeeMapper::toDomain)
                .collect(Collectors.toList()))
                .invoke(list -> LOGGER.info("Service.findAll() - found {} employees", list.size()))
                .onFailure().invoke(f -> LOGGER.error("Service.findAll() - failed", f));
        
    }

    public Uni<Employee> findById(long employeeId) {
        LOGGER.debug("Service: findById({}) - inicio", employeeId);
        
        return Uni.createFrom().item(() -> employeeRepository.findByIdOptional(employeeId))
                .map(optionalEntity -> 
                    optionalEntity.orElseThrow(() -> 
                        new NotFoundException("Employee not found with id: " + employeeId)))
                .map(entity -> employeeMapper.toDomain(entity))
                .invoke(employee -> LOGGER.info("Service: findById({}) - encontrado empleado", employeeId))
                .onFailure().invoke(f -> LOGGER.error("Service: findById({}) - error", employeeId, f));
    }

    
    public Uni<Employee> save(Employee employee)
    {
        LOGGER.debug("Service: save() - init");
        var entity = employeeMapper.toEntity(employee);

        return Uni.createFrom().item(()->{
            employeeRepository.persist(entity);
            return employeeMapper.toDomain(entity);
        })
        .invoke(saved -> LOGGER.info("Service: save() - saved employee succesfully"))
        .onFailure().invoke(f -> LOGGER.error("Service: save() - failed", f));
    }

    @Transactional
    public void update(long employeeId, Employee employee)
    {
        Optional<EmployeeEntity> OptionalEmployeeEntity = employeeRepository.findByIdOptional(employeeId);

        if(OptionalEmployeeEntity.isEmpty())
        {
            throw new NotFoundException(String.format("No Employee found with employeeId[%s] " + employee.getEmployeeId()));
        }

        EmployeeEntity employeeEntity = OptionalEmployeeEntity.get();

        employeeEntity.setEmployeeId(employeeId);
        employeeEntity.setFirstName(employee.getFirstName());
        employeeEntity.setMiddleName(employee.getMiddleName());
        employeeEntity.setLastName(employee.getLastName());
        employeeEntity.setDepartment(employee.getDepartment());
        employeeEntity.setEmailAddress(employee.getEmailAddress());
        employeeEntity.setPhoneNumber(employee.getPhoneNumber());

        employeeRepository.persist(employeeEntity);
    }

    @Transactional
    public void delete(Employee employee)
    {
        EmployeeEntity employeeEntity = employeeMapper.toEntity(employee);
        employeeRepository.delete(employeeEntity);
    }
}
