package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.CustomerRequest;
import com.adriano.risk_api.dto.CustomerResponse;
import com.adriano.risk_api.entity.Customer;
import com.adriano.risk_api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;

    public CustomerResponse create(CustomerRequest request) {

        Customer customer = new Customer();

        customer.setExternalId(request.getExternalId());
        customer.setName(request.getName());
        customer.setBirthDate(request.getBirthDate());
        customer.setEmail(request.getEmail());

        Customer saved = repository.save(customer);

        return toResponse(saved);
    }

    public CustomerResponse getById(Long id){

        Customer customer = repository.findById(id).orElseThrow(() -> new RuntimeException(
                "Customer not found: " + id));

        return toResponse(customer);
    }

    public List<CustomerResponse> getAll(){
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse getByExternalId(String externalId) {

        Customer customer = repository.findByExternalId(externalId)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        return toResponse(customer);
    }


    private CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .externalId(customer.getExternalId())
                .name(customer.getName())
                .birthDate(customer.getBirthDate())
                .email(customer.getEmail())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
