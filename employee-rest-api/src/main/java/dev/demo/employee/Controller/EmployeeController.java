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
        @Path("getAll")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Get All Employees", content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.ARRAY, implementation = Employee.class)))
    })

    public Uni<Response> getAllEmployees() {
        LOGGER.debug("Starting request to fetch all employees");
        return employeeService.findAll()
        .onItem().transform(employees -> Response.ok(employees).build())
        .onFailure().invoke(f -> LOGGER.error("Controller: getAllEmployees() - Error fetching all employees", f))
        .onFailure().recoverWithItem(f -> {
            return Response.serverError()
                           .entity(new ErrorResponse("Internal Server Error while fetching employees.", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
                           .build();
        });
    }

    @GET
    @Path("getById/{employeeId}")
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
                .onItem().transform(employee -> {
                    LOGGER.info("Found employee with ID: {}", employeeId);
                    return Response.ok(employee).build();
                })
                .onFailure(NotFoundException.class).recoverWithItem(f -> {
                    LOGGER.warn("Controller: getEmployeeById({}) - employee not found", employeeId);
                    return Response.status(Response.Status.NOT_FOUND)
                                   .entity(new ErrorResponse(f.getMessage(), Response.Status.NOT_FOUND.getStatusCode()))
                                   .build();
                })
                .onFailure().invoke(f -> LOGGER.error("Controller: getEmployeeById({}) - error fetching employee", employeeId, f))
                .onFailure().recoverWithItem(f -> {
                    return Response.serverError()
                                   .entity(new ErrorResponse("Internal Server Error", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
                                   .build();
                });
    }

    @POST
      @Path("/add")
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
                .onItem().transform(savedEmployee -> Response.status(Response.Status.CREATED).entity(savedEmployee).build())
                // Usamos invoke() para el efecto secundario de registrar el error.
                .onFailure().invoke(f -> LOGGER.error("Controller: createEmployee() - error creating employee", f))
                // Usamos recoverWithItem() para transformar el fallo en una respuesta HTTP de error.
                .onFailure().recoverWithItem(f -> {
                    ErrorResponse error = new ErrorResponse("Error creating employee. It might already exist or data is invalid.", Response.Status.BAD_REQUEST.getStatusCode());
                    return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
                });

    }

    @PUT
    @Path("/update/{employeeId}")
    @Operation(summary = "Updates an existing employee")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

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
                })
                .onFailure().recoverWithItem(f -> {
                    LOGGER.error("Controller: updateEmployee({}) - unexpected error updating employee", employeeId, f);
                    return Response.serverError()
                                   .entity(new ErrorResponse("Internal Server Error", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
                                   .build();
                });
    }


    @DELETE
    @Path("/{employeeId}")
    @Operation(summary = "Delete an employee by ID")
    @APIResponses(
            value = {
                    @APIResponse(responseCode = "204", description = "Employee deleted successfully"),
                    @APIResponse(
                            responseCode = "404",
                            description = "Employee not found",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))),
            }
    )
    public Uni<Response> deleteEmployee(@PathParam("employeeId") Long employeeId){
        LOGGER.debug("Controller: deleteEmployee({}) - start", employeeId);
        return employeeService.deleteById(employeeId)
        .onItem().transform(deleted -> {
            if(deleted){
                LOGGER.info("Controller: deleteEmployee({}) - employee deleted successfully", employeeId);
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                LOGGER.warn("Controller: deleteEmployee({}) - employee not found", employeeId);
                return Response.status(Response.Status.NOT_FOUND)
                               .entity(new ErrorResponse("Employee not found with id: " + employeeId, Response.Status.NOT_FOUND.getStatusCode()))
                               .build();
            }
        })
        .onFailure().recoverWithItem(f -> {
            LOGGER.error("Controller: deleteEmployee({}) - unexpected error deleting employee", employeeId, f);
            return Response.serverError()
                           .entity(new ErrorResponse("Internal Server Error", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()))
                           .build();
        });
    }
}