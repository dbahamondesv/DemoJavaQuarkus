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
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
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
    public Response getEmployeeById(@PathParam("employeeId") Long employeeId) {
        Optional<Employee> optionalEmployee = employeeService.findById(employeeId);

        if(optionalEmployee.isPresent()){
            LOGGER.info("Found employee " + optionalEmployee.get());
            return Response.ok(optionalEmployee.get()).build();
        }
        else{
            LOGGER.debug("No employee found with id " + employeeId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
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
    @Operation(summary = "Delete an employee")
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description = "Employee deleted successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(type = SchemaType.OBJECT, implementation = Employee.class))),
                    @APIResponse(
                            responseCode = "404",
                            description = "Employee not found",
                            content = @Content(mediaType = "application/json")),
            }
    )
    public Response deleteEmployee(@RequestBody(required = true) @Valid Employee employee) {
        Optional<Employee> optionalEmployee = employeeService.findById(employee.getEmployeeId());

        if(optionalEmployee.isPresent()){
            employeeService.delete(employee);
            LOGGER.info("Employee deleted with id " + employee.getEmployeeId());
            return Response.ok().build();
        }
        else{
            LOGGER.debug("No employee found with id " + employee.getEmployeeId());
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
