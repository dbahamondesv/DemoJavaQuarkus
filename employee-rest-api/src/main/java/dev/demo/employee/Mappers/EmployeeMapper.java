package dev.demo.employee.Mappers;

import org.mapstruct.Mapper;

import dev.demo.employee.Entity.EmployeeEntity;
import dev.demo.employee.Model.Employee;

@Mapper(componentModel = "cdi")
public interface EmployeeMapper {

    EmployeeEntity toEntity(Employee domain);
    Employee toDomain(EmployeeEntity entity);
    
}
