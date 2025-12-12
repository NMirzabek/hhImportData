package com.example.hhimportdata

import org.springframework.data.jpa.repository.JpaRepository


interface VacancyRepository : JpaRepository<Vacancy, Long> {
    fun findAllByHhIdIn(hhIds: Collection<String>): List<Vacancy>
}
