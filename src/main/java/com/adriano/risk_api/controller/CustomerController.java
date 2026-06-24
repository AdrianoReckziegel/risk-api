package com.adriano.risk_api.controller;

import com.adriano.risk_api.dto.CustomerRequest;
import com.adriano.risk_api.dto.CustomerResponse;
import com.adriano.risk_api.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Customers", description = "Customer management operations")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return service.create(request);
    }

    @Operation(
            summary = "Get customer by ID",
            description = "Retrieves a customer by their ID"
    )
    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(
            summary = "Get all customers",
            description = "Retrieves all customers"
    )
    @GetMapping
    public List<CustomerResponse> getAll() {
        return service.getAll();
    }

    @Operation(
            summary = "Get customer by external ID",
            description = "Retrieves a customer by their external ID"
    )
    @GetMapping("/external/{externalId}")
    public CustomerResponse getByExternalId(@PathVariable String externalId) {
        return service.getByExternalId(externalId);
    }

    @Operation(
            summary = "Update customer",
            description = "Updates an existing customer by their ID"
    )
    @PutMapping("/{id}")
    public CustomerResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {

        return service.update(id, request);
    }

    @Operation(
            summary = "Delete customer",
            description = "Deletes a customer by their ID"
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

}
