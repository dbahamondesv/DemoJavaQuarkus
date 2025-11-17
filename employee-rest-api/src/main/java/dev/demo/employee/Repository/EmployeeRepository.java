package dev.demo.employee.Repository;
import dev.demo.employee.Entity.EmployeeEntity;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;

@ApplicationScoped
public class EmployeeRepository implements PanacheRepositoryBase<EmployeeEntity, Long> {

}