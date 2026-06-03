package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.CustomerRequest;
import com.adriano.risk_api.dto.CustomerResponse;
import com.adriano.risk_api.entity.Customer;
import com.adriano.risk_api.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
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

        return CustomerResponse.builder()
                .id(saved.getId())
                .externalId(saved.getExternalId())
                .name(saved.getName())
                .birthDate(saved.getBirthDate())
                .email(saved.getEmail())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public CustomerResponse getById(Long id){

        Customer customer = repository.findById(id).orElseThrow(() -> new RuntimeException(
                "Customer not found: " + id));

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
