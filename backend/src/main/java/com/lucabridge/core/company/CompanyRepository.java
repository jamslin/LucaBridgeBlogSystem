package com.lucabridge.core.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Short> {

    @Query("SELECT c FROM Company c LEFT JOIN FETCH c.logoMedia LEFT JOIN FETCH c.text WHERE c.id = 1")
    Optional<Company> findSingleton();
}
