package dev.demo.employee.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import dev.demo.employee.Entity.EmployeeEntity;
import dev.demo.employee.Model.Employee;

@Mapper(componentModel = "cdi")
public interface EmployeeMapper {

    EmployeeEntity toEntity(Employee domain);
    Employee toDomain(Employee employee1);
    void updateEntityFromDomain(Employee source, @MappingTarget EmployeeEntity target);

}
