package com.smartpark.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartpark.api.entity.Vaga;

@Repository
public interface VagaRepository extends JpaRepository<Vaga, Long> {
}