package com.mongodb.kitchensink.repository;

import com.mongodb.kitchensink.config.EmbeddedMongoConfig;
import com.mongodb.kitchensink.model.Address;
import com.mongodb.kitchensink.model.Profile;
import com.mongodb.kitchensink.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
@Import(EmbeddedMongoConfig.class)
public class AllRepositoryTests {
    @Autowired
    private ProfileRepository profileRepository;

    private Profile profile1, profile2, profile3;

    @Autowired
    private UserRepository userRepository;

    private User user1, user2, user3, user4, user5;
    @Test
    void dummyTest() {
        assertThat(true).isTrue();
    }
    @Test
    void contextLoads() {
        assertThat(userRepository).isNotNull();
        assertThat(profileRepository).isNotNull();
    }
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user1 = new User();
        user1.setEmail("john.doe@example.com");
        user1.setUsername("johndoe");
        user1.setActive(true);
        user1.setAccountVerificationPending(false);
        user1.setFirstLogin(true);
        user1.setCreatedAt(Instant.now().minus(10, ChronoUnit.DAYS));
        user1.setRoles(List.of("USER"));

        user2 = new User();
        user2.setEmail("jane.doe@example.com");
        user2.setUsername("janedoe");
        user2.setActive(true);
        user2.setAccountVerificationPending(false);
        user2.setFirstLogin(false);
        user2.setCreatedAt(Instant.now().minus(5, ChronoUnit.DAYS));
        user2.setRoles(List.of("ADMIN", "USER"));

        user3 = new User();
        user3.setEmail("petra.parker@example.com");
        user3.setUsername("petra_p");
        user3.setActive(false);
        user3.setAccountVerificationPending(true);
        user3.setFirstLogin(true);
        user3.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        user3.setRoles(List.of("USER"));

        user4 = new User();
        user4.setEmail("test.user@example.com");
        user4.setUsername("testuser");
        user4.setActive(true);
        user4.setAccountVerificationPending(false);
        user4.setFirstLogin(false);
        user4.setCreatedAt(Instant.now());
        user4.setRoles(List.of("USER"));

        user5 = new User();
        user5.setEmail("admin.user@example.com");
        user5.setUsername("adminuser");
        user5.setActive(true);
        user5.setAccountVerificationPending(false);
        user5.setFirstLogin(false);
        user5.setCreatedAt(Instant.now());
        user5.setRoles(List.of("ADMIN"));

        userRepository.saveAll(List.of(user1, user2, user3, user4, user5));
        profileRepository.deleteAll();

        // Address for johndoe and petra (Same City and Country)
        Address sharedAddress = Address.builder()
                .country("USA")
                .state("CA")
                .pincode("12232")
                .street("123 ST")
                .city("Anytown")
                .build();

        // Address for janedoe (Different City and Country)
        Address uniqueAddress = Address.builder()
                .country("Canada")
                .state("ON")
                .pincode("K1A 0B1")
                .street("456 Blvd")
                .city("Toronto")
                .build();

        profile1 = new Profile();
        profile1.setUsername("johndoe");
        profile1.setEmail("john.doe@example.com");
        profile1.setFirstName("John");
        profile1.setLastName("Doe");
        profile1.setAddress(sharedAddress);

        profile2 = new Profile();
        profile2.setUsername("janedoe");
        profile2.setEmail("jane.doe@example.com");
        profile2.setFirstName("Jane");
        profile2.setLastName("Doe");
        profile2.setAddress(uniqueAddress);

        profile3 = new Profile();
        profile3.setUsername("petra");
        profile3.setEmail("petra.l@example.com");
        profile3.setFirstName("Petra");
        profile3.setLastName("Parker");
        profile3.setAddress(sharedAddress);

        profileRepository.saveAll(List.of(profile1, profile2, profile3));
    }
    @Test
    @DisplayName("should find a profile by existing username")
    void shouldFindProfileByUsername() {
        Optional<Profile> foundProfile = profileRepository.findByUsername("johndoe");
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getUsername()).isEqualTo("johndoe");
    }

    @Test
    @DisplayName("should return empty optional for non-existing username")
    void shouldNotFindProfileByNonExistingUsername() {
        Optional<Profile> foundProfile = profileRepository.findByUsername("unknown_user");
        assertThat(foundProfile).isEmpty();
    }

    @Test
    @DisplayName("should find a profile by existing email")
    void shouldFindProfileByEmail() {
        Optional<Profile> foundProfile = profileRepository.findByEmail("jane.doe@example.com");
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getEmail()).isEqualTo("jane.doe@example.com");
    }

    @Test
    @DisplayName("should return empty optional for non-existing email")
    void shouldNotFindProfileByNonExistingEmail() {
        Optional<Profile> foundProfile = profileRepository.findByEmail("nonexistent@example.com");
        assertThat(foundProfile).isEmpty();
    }

    @Test
    @DisplayName("should find profiles by country ignoring case")
    void shouldFindProfilesByCountryIgnoringCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Profile> profilesInUSA = profileRepository.findByAddress_CountryContainingIgnoreCase("usa", pageable);
        assertThat(profilesInUSA.getTotalElements()).isEqualTo(2);
        assertThat(profilesInUSA.getContent()).extracting(Profile::getUsername)
                .containsExactlyInAnyOrder("johndoe", "petra");
    }

    @Test
    @DisplayName("should find no profiles for a non-existing country")
    void shouldFindNoProfilesForNonExistingCountry() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Profile> profilesInUK = profileRepository.findByAddress_CountryContainingIgnoreCase("uk", pageable);
        assertThat(profilesInUK.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("should find profiles by first or last name ignoring case")
    void shouldFindProfilesByFirstNameOrLastName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Profile> profiles = profileRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("john", "doe", pageable);
        assertThat(profiles.getTotalElements()).isEqualTo(2);
        assertThat(profiles.getContent()).extracting(Profile::getUsername)
                .containsExactlyInAnyOrder("johndoe", "janedoe");
    }

    @Test
    @DisplayName("should return empty page when no matching names are found")
    void shouldReturnEmptyPageForNoMatchingNames() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Profile> profiles = profileRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("alex", "smith", pageable);
        assertThat(profiles.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("should find profiles by city ignoring case")
    void shouldFindProfilesByCityIgnoringCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Profile> profilesInAnytown = profileRepository.findByAddress_CityContainingIgnoreCase("anytown", pageable);
        assertThat(profilesInAnytown.getTotalElements()).isEqualTo(2);
        assertThat(profilesInAnytown.getContent()).extracting(Profile::getUsername)
                .containsExactlyInAnyOrder("johndoe", "petra");
    }

    @Test
    @DisplayName("should find no profiles for a non-existing city")
    void shouldFindNoProfilesForNonExistingCity() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Profile> profilesInLondon = profileRepository.findByAddress_CityContainingIgnoreCase("london", pageable);
        assertThat(profilesInLondon.isEmpty()).isTrue();
    }
    @Test
    @DisplayName("should find a user by email")
    void shouldFindUserByEmail() {
        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("should check if user with given email exists")
    void shouldCheckIfEmailExists() {
        boolean exists = userRepository.existsByEmail("jane.doe@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("should check if user with non-existing email does not exist")
    void shouldCheckIfNonExistingEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("should find a user by username")
    void shouldFindUserByUsername() {
        Optional<User> foundUser = userRepository.findByUsername("johndoe");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("johndoe");
    }

    @Test
    @DisplayName("should check if user with given username exists")
    void shouldCheckIfUsernameExists() {
        boolean exists = userRepository.existsByUsername("janedoe");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("should find users by email containing substring ignoring case")
    void shouldFindUsersByEmailContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> users = userRepository.findByEmailContainingIgnoreCase("doe", pageable);
        assertThat(users.getTotalElements()).isEqualTo(2);
        assertThat(users.getContent()).extracting(User::getEmail)
                .containsExactlyInAnyOrder("john.doe@example.com", "jane.doe@example.com");
    }

    @Test
    @DisplayName("should find users by username containing substring ignoring case")
    void shouldFindUsersByUsernameContainingIgnoreCase() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> users = userRepository.findByUsernameContainingIgnoreCase("doe", pageable);
        assertThat(users.getTotalElements()).isEqualTo(2);
        assertThat(users.getContent()).extracting(User::getUsername)
                .containsExactlyInAnyOrder("johndoe", "janedoe");
    }

    @Test
    @DisplayName("should count all active users")
    void shouldCountActiveUsers() {
        long count = userRepository.countByActiveTrue();
        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("should count users with pending account verification")
    void shouldCountUsersWithPendingVerification() {
        long count = userRepository.countByIsAccountVerificationPendingTrue();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("should count users with first login status")
    void shouldCountUsersWithFirstLoginStatus() {
        long count = userRepository.countByIsFirstLoginTrue();
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("should count users with a specific role")
    void shouldCountUsersByRole() {
        long count = userRepository.countByRoles("USER");
        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("should count users with exact roles match")
    void shouldCountUsersWithExactRoles() {
        long count = userRepository.countByExactRoles(List.of("ADMIN", "USER"));
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("should count users created after a specific date")
    void shouldCountUsersCreatedAfterDate() {
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        long count = userRepository.countByCreatedAtAfter(twoDaysAgo);
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("should count users with multiple roles")
    void shouldCountUsersWithMultipleRoles() {
        long count = userRepository.countUsersWithRoles("ADMIN", "USER");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("should find users whose emails are in a given list")
    void shouldFindUsersByEmailInList() {
        List<String> emails = List.of("john.doe@example.com", "jane.doe@example.com", "nonexistent@example.com");
        List<User> foundUsers = userRepository.findByEmailIn(emails);
        assertThat(foundUsers).hasSize(2);
        assertThat(foundUsers).extracting(User::getEmail)
                .containsExactlyInAnyOrder("john.doe@example.com", "jane.doe@example.com");
    }

    @Test
    @DisplayName("should return empty list for no matching emails")
    void shouldReturnEmptyListForNoMatchingEmails() {
        List<String> emails = List.of("no-match1@test.com", "no-match2@test.com");
        List<User> foundUsers = userRepository.findByEmailIn(emails);
        assertThat(foundUsers).isEmpty();
    }
}