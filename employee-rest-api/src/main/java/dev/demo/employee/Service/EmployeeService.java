package dev.demo.employee.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dev.demo.employee.Entity.EmployeeEntity;
import dev.demo.employee.Mappers.EmployeeMapper;
import dev.demo.employee.Model.Employee;
import dev.demo.employee.Repository.EmployeeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    //Constructor Injection
    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;  
    }

    public List<Employee> findAll()
    {
        return employeeRepository.findAll()
                .stream()
                .map(employeeMapper::toDomain)
                .collect(Collectors.toList());
    }

    public Optional<Employee> findById(long employeeId)
    {
            return employeeRepository.findByIdOptional(employeeId)
                   .map(employeeMapper::toDomain);
    }

    @Transactional
    public void save(Employee employee)
    {
        EmployeeEntity employeeEntity = employeeMapper.toEntity(employee);
        employeeRepository.persist(employeeEntity);
    }

    @Transactional
    public void update(long employeeId, Employee employee)
    {
        Optional<EmployeeEntity> OptionalEmployeeEntity = employeeRepository.findByIdOptional(employeeId);

        if(OptionalEmployeeEntity.isEmpty())
        {
            throw new NotFoundException(String.format("No Employee found with employeeId[%s] " + employee.geEmployeeId()));
        }

        EmployeeEntity employeeEntity = OptionalEmployeeEntity.get();

        employeeEntity.setEmployeeId(employeeId);
        employeeEntity.setFirstName(employee.getFirstName());
        employeeEntity.setMidddleName(employee.getMidddleName());
        employeeEntity.setLastName(employee.getLastName());
        employeeEntity.setDepartment(employee.getDepartment());
        employeeEntity.setEmailAddress(employee.getEmail());
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
