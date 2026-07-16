package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.CustomerRequest;
import com.adriano.risk_api.dto.CustomerResponse;
import com.adriano.risk_api.entity.Customer;
import com.adriano.risk_api.exception.CustomerNotFoundException;
import com.adriano.risk_api.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerService service;

    @Test
    void shouldGetCustomerById() {

        Customer customer = customer();

        when(repository.findById(1L))
                .thenReturn(Optional.of(customer));

        CustomerResponse response = service.getById(1L);

        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());

        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenCustomerDoesNotExist() {

        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                CustomerNotFoundException.class,
                () -> service.getById(99L));

        verify(repository).findById(99L);
    }

    @Test
    void shouldCreateCustomer() {

        Customer customer = customer();

        when(repository.save(any(Customer.class)))
                .thenReturn(customer);

        CustomerResponse response =
                service.create(request());

        assertEquals("John Doe", response.getName());

        verify(repository).save(any(Customer.class));
    }


    private Customer customer() {

        Customer customer = new Customer();

        customer.setId(1L);
        customer.setExternalId("CUST-001");
        customer.setName("John Doe");
        customer.setBirthDate(LocalDate.of(1990,1,1));
        customer.setEmail("john@email.com");
        customer.setCreditScore(750);
        customer.setAnnualIncome(BigDecimal.valueOf(100000));
        customer.setCreatedAt(Instant.now());

        return customer;
    }

    private CustomerRequest request() {

        CustomerRequest request = new CustomerRequest();

        request.setExternalId("CUST-001");
        request.setName("John Doe");
        request.setBirthDate(LocalDate.of(1990,1,1));
        request.setEmail("john@email.com");
        request.setCreditScore(750);
        request.setAnnualIncome(BigDecimal.valueOf(100000));

        return request;
    }


}
