package dev.demo.employee.Controller;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import dev.demo.employee.Model.Employee;
import dev.demo.employee.Service.EmployeeService;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
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

}
