package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.DashboardStatsResponse;
import com.mongodb.kitchensink.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class DashboardService {
    @Autowired
    private final UserRepository userRepository;

    public DashboardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        long pendingVerifications = userRepository.countByIsAccountVerificationPendingTrue();
        long firstTimeLogins = userRepository.countByIsFirstLoginTrue();
        long adminUsers = userRepository.countByRolesContaining("ADMIN");
        long regularUsers = userRepository.countByRolesContaining("USER");
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        Instant startOfMonth = firstDayOfMonth.atStartOfDay().toInstant(ZoneOffset.UTC);
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(startOfMonth);

        return new DashboardStatsResponse(
                totalUsers,
                activeUsers,
                pendingVerifications,
                firstTimeLogins,
                newUsersThisMonth,
                adminUsers,
                regularUsers
        );
    }
}
