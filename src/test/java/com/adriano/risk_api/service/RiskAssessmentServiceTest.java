package com.adriano.risk_api.service;

import com.adriano.risk_api.dto.CreateRiskAssessmentRequest;
import com.adriano.risk_api.dto.RiskAssessmentResponse;
import com.adriano.risk_api.dto.RiskResult;
import com.adriano.risk_api.entity.Customer;
import com.adriano.risk_api.entity.Decision;
import com.adriano.risk_api.entity.RiskAssessment;
import com.adriano.risk_api.entity.RiskLevel;
import com.adriano.risk_api.exception.CustomerNotFoundException;
import com.adriano.risk_api.exception.RiskAssessmentNotFoundException;
import com.adriano.risk_api.repository.CustomerRepository;
import com.adriano.risk_api.repository.RiskAssessmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceTest {

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RiskScoringService riskScoringService;

    @InjectMocks
    private RiskAssessmentServiceImpl service;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(10L);
        testCustomer.setExternalId("CUST-123");
        testCustomer.setName("Alice");
        testCustomer.setCreditScore(780);
        testCustomer.setAnnualIncome(BigDecimal.valueOf(110000));
    }

    private RiskAssessment createSavedAssessment(Customer customer, int score, RiskLevel level, Decision decision) {
        RiskAssessment assessment = new RiskAssessment();
        assessment.setId(100L);
        assessment.setCustomer(customer);
        assessment.setAssessmentDate(LocalDate.now());
        assessment.setRiskScore(score);
        assessment.setRiskLevel(level);
        assessment.setDecision(decision);
        assessment.setInputSnapshot("{\"externalId\":\"CUST-123\",\"creditScore\":780,\"annualIncome\":110000}");
        return assessment;
    }

    @Test
    void shouldCalculateAssessmentAndPopulateAllFields() {
        when(customerRepository.findById(10L)).thenReturn(Optional.of(testCustomer));
        when(riskScoringService.calculate(testCustomer))
                .thenReturn(new RiskResult(100, RiskLevel.LOW, Decision.APPROVED));

        RiskAssessment saved = createSavedAssessment(testCustomer, 100, RiskLevel.LOW, Decision.APPROVED);
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenReturn(saved);

        RiskAssessmentResponse response = service.calculateAssessment(10L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(10L, response.getCustomerId());
        assertEquals(100, response.getRiskScore());
        assertEquals("LOW", response.getRiskLevel());
        assertEquals("APPROVED", response.getDecision());
        assertEquals(LocalDate.now(), response.getAssessmentDate());

        ArgumentCaptor<RiskAssessment> captor = ArgumentCaptor.forClass(RiskAssessment.class);
        verify(riskAssessmentRepository).save(captor.capture());
        RiskAssessment captured = captor.getValue();
        assertEquals(testCustomer, captured.getCustomer());
        assertEquals(100, captured.getRiskScore());
        assertEquals(RiskLevel.LOW, captured.getRiskLevel());
        assertEquals(Decision.APPROVED, captured.getDecision());
        assertNotNull(captured.getInputSnapshot());
        assertTrue(captured.getInputSnapshot().contains("CUST-123"));
    }

    @Test
    void shouldThrowCustomerNotFoundWhenCalculatingForUnknownCustomer() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> service.calculateAssessment(999L));
        verify(riskAssessmentRepository, never()).save(any());
    }

    @Test
    void shouldCreateAssessmentViaRequest() {
        CreateRiskAssessmentRequest request = new CreateRiskAssessmentRequest();
        request.setCustomerId(10L);

        when(customerRepository.findById(10L)).thenReturn(Optional.of(testCustomer));
        when(riskScoringService.calculate(testCustomer))
                .thenReturn(new RiskResult(60, RiskLevel.MEDIUM, Decision.REVIEW));

        RiskAssessment saved = createSavedAssessment(testCustomer, 60, RiskLevel.MEDIUM, Decision.REVIEW);
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenReturn(saved);

        RiskAssessmentResponse response = service.createAssessment(request);

        assertNotNull(response);
        assertEquals("MEDIUM", response.getRiskLevel());
        assertEquals("REVIEW", response.getDecision());
        assertEquals(60, response.getRiskScore());
    }

    @Test
    void shouldGetAssessmentById() {
        RiskAssessment assessment = createSavedAssessment(testCustomer, 100, RiskLevel.LOW, Decision.APPROVED);
        when(riskAssessmentRepository.findById(100L)).thenReturn(Optional.of(assessment));

        RiskAssessmentResponse response = service.getById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("APPROVED", response.getDecision());
        assertEquals(LocalDate.now(), response.getAssessmentDate());
    }

    @Test
    void shouldThrowWhenAssessmentNotFoundById() {
        when(riskAssessmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RiskAssessmentNotFoundException.class, () -> service.getById(999L));
    }

    @Test
    void shouldGetAllAssessments() {
        RiskAssessment a1 = createSavedAssessment(testCustomer, 100, RiskLevel.LOW, Decision.APPROVED);
        RiskAssessment a2 = createSavedAssessment(testCustomer, 30, RiskLevel.HIGH, Decision.REJECTED);
        a2.setId(101L);

        when(riskAssessmentRepository.findAll()).thenReturn(List.of(a1, a2));

        List<RiskAssessmentResponse> all = service.getAll();

        assertEquals(2, all.size());
        assertEquals("APPROVED", all.get(0).getDecision());
        assertEquals("REJECTED", all.get(1).getDecision());
    }

    @Test
    void shouldGetByCustomerId() {
        when(customerRepository.findById(10L)).thenReturn(Optional.of(testCustomer));
        RiskAssessment a1 = createSavedAssessment(testCustomer, 100, RiskLevel.LOW, Decision.APPROVED);
        when(riskAssessmentRepository.findByCustomerId(10L)).thenReturn(List.of(a1));

        List<RiskAssessmentResponse> list = service.getByCustomerId(10L);

        assertEquals(1, list.size());
        assertEquals("APPROVED", list.get(0).getDecision());
        assertEquals(LocalDate.now(), list.get(0).getAssessmentDate());
    }

    @Test
    void shouldUpdateAssessment() {
        RiskAssessment existing = createSavedAssessment(testCustomer, 40, RiskLevel.HIGH, Decision.REJECTED);
        when(riskAssessmentRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(customerRepository.findById(10L)).thenReturn(Optional.of(testCustomer));
        when(riskScoringService.calculate(testCustomer))
                .thenReturn(new RiskResult(100, RiskLevel.LOW, Decision.APPROVED));

        when(riskAssessmentRepository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        CreateRiskAssessmentRequest request = new CreateRiskAssessmentRequest();
        request.setCustomerId(10L);

        RiskAssessmentResponse response = service.updateAssessment(100L, request);

        assertEquals("APPROVED", response.getDecision());
        assertEquals(100, response.getRiskScore());
        assertEquals("LOW", response.getRiskLevel());
    }

    @Test
    void shouldDeleteAssessment() {
        RiskAssessment assessment = createSavedAssessment(testCustomer, 100, RiskLevel.LOW, Decision.APPROVED);
        when(riskAssessmentRepository.findById(100L)).thenReturn(Optional.of(assessment));

        service.deleteAssessment(100L);

        verify(riskAssessmentRepository).delete(assessment);
    }
}
