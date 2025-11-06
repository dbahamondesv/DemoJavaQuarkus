package dev.demo.employee.Controller;

import java.net.URI;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import dev.demo.employee.Model.Employee;
import dev.demo.employee.Service.EmployeeService;
import dev.demo.employee.Utils.ErrorResponse;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("api/v1/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeController {

    // Logger
    public static final Logger LOGGER = LoggerFactory.getLogger(EmployeeController.class);

    public final EmployeeService employeeService;

    // Constructor Injection
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GET
    @Operation(summary = "Returns all existing employees")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Get All Employees", content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, implementation = Employee.class)))
    })

    public Response getAllEmployees() {
        return Response.ok(employeeService.findAll()).build();
    }

    @GET
    @Path("/{employeeId}")
    @Operation(summary = "Returns employee by employeeId")
    @APIResponses(
            value = {
                @APIResponse(
                    responseCode = "200",
                    description = "Get Employee by EmployeeId",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(type = SchemaType.OBJECT,
                    implementation = Employee.class))),
                @APIResponse(
                    responseCode = "404",
                    description = "Employee not found",
                    content = @Content(mediaType = "applicastion/json"))
               }
    )
    public Uni<Response> getEmployeeById(@PathParam("employeeId") Long employeeId) {

        LOGGER.debug("Starting request to fetch employee with ID: {}", employeeId);

        return Uni.createFrom().item(() -> employeeService.findById(employeeId))
            // Transformamos el Optional (o el resultado) en Response
            .onItem().transform(optionalEmployee -> { 
                if (optionalEmployee.isPresent()) {
                    Employee employee = optionalEmployee.get();
                    LOGGER.info("Found employee - ID: {}, Name: {}, Department: {}", 
                        employee.getEmployeeId(),
                        employee.getFirstName() + " " + employee.getMiddleName() + " " + employee.getLastName(),
                        employee.getDepartment());
                    return Response.ok(employee).build();
                } else {
                    LOGGER.warn("Employee not found with ID: {}", employeeId);
                    return Response.status(Response.Status.NOT_FOUND)
                                   .entity(new ErrorResponse("Employee not found", 404))
                                   .build();
                }
            })
            // Registrar errores y devolver 500 en caso de fallo inesperado
            .onFailure().invoke(t -> LOGGER.error("Error processing request for employee ID: {} - {}", employeeId, t.getMessage(), t))
            .onFailure().recoverWithItem(t -> 
                Response.serverError()
                        .entity(new ErrorResponse("Internal Server Error", 500))
                        .build()
            );
    }

    @POST
    @Operation(summary = "Adds a new employee")
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "201",
                            description = "Employee created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(type = SchemaType.OBJECT, implementation = Employee.class))),
                    @APIResponse(
                            responseCode = "400",
                            description = "Employee already exists with employeeId",
                            content = @Content(mediaType = "application/json")),
            }
    )
    public Response createEmployee(@RequestBody(required = true) @Valid Employee employee) {
        employeeService.save(employee);
        URI employeeUrl = URI.create("/api/v1/employees/" + employee.getEmployeeId());
        LOGGER.info("New employee added at URL {}" + employeeUrl);
        return Response.created(employeeUrl).build();
    }

    @PUT
    @Operation(summary = "Updates an existing employee")
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "201",
                            description = "Employee updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(type = SchemaType.OBJECT, implementation = Employee.class))),
                    @APIResponse(
                            responseCode = "404",
                            description = "Employee not found",
                            content = @Content(mediaType = "application/json")),
            }
    )
    public Response updateEmployee(@RequestBody(required = true) @Valid Employee employee) {
        Optional<Employee> optionalEmployee = employeeService.findById(employee.getEmployeeId());

        if(optionalEmployee.isPresent()){
            employeeService.update(employee.getEmployeeId(), employee);
            URI employeeUrl = URI.create("/api/v1/employees/" +  employee.getEmployeeId());
            LOGGER.info("Employee updated at URL {}" + employeeUrl);
            return Response.created(employeeUrl).build();
        }
        else{
            LOGGER.debug("No employee found with id " + employee.getEmployeeId());
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


    @DELETE
    @Path("/{employeeId}")
    @Operation(summary = "Delete an employee by ID")
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description = "Employee deleted successfully",
                            content = @Content(mediaType = "application/json")),
                    @APIResponse(
                            responseCode = "404",
                            description = "Employee not found",
                            content = @Content(mediaType = "application/json")),
            }
    )
    public Uni<Response> deleteEmployee(@PathParam("employeeId") Long employeeId){

    return employeeService.findById(employeeId)
        .onItem().transform(deleted -> {
            if(deleted != null){
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        })
        .onFailure().recoverWithItem(f -> {
            LOGGER.error("Controller: deleteEmployee({}) - error deleting employee", employeeId, f);
            return Response.serverError().build();
        });
    }
}