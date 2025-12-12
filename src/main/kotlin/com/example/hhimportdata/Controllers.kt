package com.example.hhimportdata

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vacancies")
class VacancyController(
    private val vacancyService: VacancyService
) {

    data class FetchResponse(val message: String)

    // POST /api/vacancies/fetch?keyword=java&pages=1
    @PostMapping("/fetch")
    fun fetch(
        @RequestParam keyword: String,
        @RequestParam(required = false, defaultValue = "1") pages: Int
    ): FetchResponse {
        val saved = vacancyService.fetchAndStore(keyword, pagesToLoad = pages)
        return FetchResponse("Fetched and stored $saved vacancies for keyword '$keyword'")
    }

    // GET /api/vacancies/{id}
    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): VacancyDto =
        vacancyService.getById(id)

    // GET /api/vacancies/search?keyword=kotlin&workFormat=REMOTE
    @GetMapping("/search")
    fun search(
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) minSalary: Long?,
        @RequestParam(required = false) maxSalary: Long?,
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false) workFormat: WorkFormat?
    ): List<VacancyDto> {
        val filter = VacancySearchFilter(
            keyword = keyword,
            minSalary = minSalary,
            maxSalary = maxSalary,
            city = city,
            workFormat = workFormat
        )
        return vacancyService.search(filter)
    }
}
