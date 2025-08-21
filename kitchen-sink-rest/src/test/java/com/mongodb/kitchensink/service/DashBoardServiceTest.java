package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.DashboardStatsResponse;
import com.mongodb.kitchensink.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Tests")
class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("getDashboardStats should return correct stats when data exists")
    void getDashboardStats_shouldReturnCorrectStatsWhenDataExists() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByActiveTrue()).thenReturn(80L);
        when(userRepository.countByIsAccountVerificationPendingTrue()).thenReturn(5L);
        when(userRepository.countByIsFirstLoginTrue()).thenReturn(15L);
        when(userRepository.countByRolesContaining("ADMIN")).thenReturn(10L);
        when(userRepository.countByRolesContaining("USER")).thenReturn(90L);

        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        Instant startOfMonth = firstDayOfMonth.atStartOfDay().toInstant(ZoneOffset.UTC);
        when(userRepository.countByCreatedAtAfter(startOfMonth)).thenReturn(25L);

        // When
        DashboardStatsResponse stats = dashboardService.getDashboardStats();

        // Then
        assertEquals(100L, stats.getTotalUsers());
        assertEquals(80L, stats.getActiveUsers());
        assertEquals(5L, stats.getPendingVerifications());
        assertEquals(15L, stats.getFirstTimeLogins());
        assertEquals(10L, stats.getAdminUsers());
        assertEquals(90L, stats.getRegularUsers());
        assertEquals(25L, stats.getNewUsersThisMonth());

        // Verify that all repository methods were called exactly once
        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).countByActiveTrue();
        verify(userRepository, times(1)).countByIsAccountVerificationPendingTrue();
        verify(userRepository, times(1)).countByIsFirstLoginTrue();
        verify(userRepository, times(1)).countByRolesContaining("ADMIN");
        verify(userRepository, times(1)).countByRolesContaining("USER");
        verify(userRepository, times(1)).countByCreatedAtAfter(startOfMonth);
    }

    @Test
    @DisplayName("getDashboardStats should return zero stats when repository is empty")
    void getDashboardStats_shouldReturnZeroStatsWhenRepositoryIsEmpty() {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countByActiveTrue()).thenReturn(0L);
        when(userRepository.countByIsAccountVerificationPendingTrue()).thenReturn(0L);
        when(userRepository.countByIsFirstLoginTrue()).thenReturn(0L);
        when(userRepository.countByRolesContaining("ADMIN")).thenReturn(0L);
        when(userRepository.countByRolesContaining("USER")).thenReturn(0L);

        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        Instant startOfMonth = firstDayOfMonth.atStartOfDay().toInstant(ZoneOffset.UTC);
        when(userRepository.countByCreatedAtAfter(startOfMonth)).thenReturn(0L);

        // When
        DashboardStatsResponse stats = dashboardService.getDashboardStats();

        // Then
        assertEquals(0L, stats.getTotalUsers());
        assertEquals(0L, stats.getActiveUsers());
        assertEquals(0L, stats.getPendingVerifications());
        assertEquals(0L, stats.getFirstTimeLogins());
        assertEquals(0L, stats.getAdminUsers());
        assertEquals(0L, stats.getRegularUsers());
        assertEquals(0L, stats.getNewUsersThisMonth());

        // Verify that all repository methods were called exactly once
        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).countByActiveTrue();
        verify(userRepository, times(1)).countByIsAccountVerificationPendingTrue();
        verify(userRepository, times(1)).countByIsFirstLoginTrue();
        verify(userRepository, times(1)).countByRolesContaining("ADMIN");
        verify(userRepository, times(1)).countByRolesContaining("USER");
        verify(userRepository, times(1)).countByCreatedAtAfter(startOfMonth);
    }
}