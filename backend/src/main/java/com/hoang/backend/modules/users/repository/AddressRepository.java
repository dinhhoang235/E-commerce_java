package com.hoang.backend.modules.users.repository;

/**
 * Repository thao tác dữ liệu Address.
 */

import com.hoang.backend.modules.users.entity.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findAllByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    Optional<Address> findFirstByUserIdAndAddressLine1IgnoreCaseAndCityIgnoreCaseAndStateIgnoreCaseAndZipCodeIgnoreCaseAndCountryIgnoreCase(
            Long userId,
            String addressLine1,
            String city,
            String state,
            String zipCode,
            String country
    );
}