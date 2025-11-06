package dev.demo.employee.Controller;

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
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
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
    @Inject
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GET
    @Operation(summary = "Returns all existing employees")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Get All Employees", content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, implementation = Employee.class)))
    })

    public Uni<Response> getAllEmployees() {
        LOGGER.debug("Starting request to fetch all employees");
        return employeeService.findAll()
        .onItem().transform(employees -> Response.ok(employees).build())
        .onFailure().recoverWithItem(f -> {
            LOGGER.error("Error fetching all employees", f);
            return Response.serverError()
                           .entity(new ErrorResponse("Internal Server Error", 500))
                           .build();
        });
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
        return employeeService.findById(employeeId)
                .onItem().transformToUni(optionalEmployee -> {
                    if (optionalEmployee != null) {
                        Employee employee = optionalEmployee;
                        LOGGER.info("Found employee - ID: {}, Name: {}, Department: {}",
                                employee.getEmployeeId(),
                                employee.getFirstName() + " " + employee.getMiddleName() + " " + employee.getLastName(),
                                employee.getDepartment());
                        return Uni.createFrom().item(Response.ok(employee).build());
                    } else {
                        LOGGER.warn("Employee not found with ID: {}", employeeId);
                        return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND)
                                .entity(new ErrorResponse("Employee not found", 404))
                                .build());
                    }
                })
                .onFailure().recoverWithItem(f -> Response.serverError()
                        .entity(new ErrorResponse("Internal Server Error", 500)).build());
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
    public Uni<Response> createEmployee(@RequestBody(required = true) @Valid Employee employee) {

        LOGGER.debug("Controller: createEmployee() - start");
        return employeeService.save(employee)
                .onItem().transform(savedEmployee -> Response.ok(savedEmployee)
                .status(Response.Status.CREATED).build())
                .onFailure().invoke(f -> LOGGER.error("Controller: createEmployee() - error creating employee", f))
                .onFailure().recoverWithItem(f -> {
                    LOGGER.warn("Controller: createEmployee() - employee already exists", f);
                    return Response.status(Response.Status.BAD_REQUEST)
                                   .entity(new ErrorResponse("Employee already exists", Response.Status.BAD_REQUEST.getStatusCode()))
                                   .build();
                });

    }

    @PUT
    @Path("/{employeeId}")
    @Operation(summary = "Updates an existing employee")
    @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description = "Employee updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(type = SchemaType.OBJECT, implementation = Employee.class))),
                    @APIResponse(
                            responseCode = "404",
                            description = "Employee not found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))),
            }
    )
    public Uni<Response> updateEmployee(@PathParam("employeeId") Long employeeId, @RequestBody(required = true) @Valid Employee employee) {
        LOGGER.debug("Controller: updateEmployee({}) - start", employeeId);
        return employeeService.update(employeeId, employee)
                .onItem().transform(updatedEmployee -> {
                    LOGGER.info("Controller: updateEmployee({}) - employee updated successfully", employeeId);
                    return Response.ok(updatedEmployee).build();
                })
                .onFailure(NotFoundException.class).recoverWithItem(f -> {
                    LOGGER.warn("Controller: updateEmployee({}) - employee not found", employeeId);
                    return Response.status(Response.Status.NOT_FOUND)
                                   .entity(new ErrorResponse(f.getMessage(), Response.Status.NOT_FOUND.getStatusCode()))
                                   .build();
                });
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