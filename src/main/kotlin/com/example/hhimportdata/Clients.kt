package com.example.hhimportdata

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.getForObject
import org.springframework.web.util.UriComponentsBuilder

@Component
class HhClient(
    builder: RestTemplateBuilder
) {
    private val restTemplate = builder.build()

    fun searchVacancies(keyword: String, page: Int, perPage: Int): HhVacanciesResponse {
        val uri = UriComponentsBuilder
            .fromHttpUrl("https://api.hh.ru/vacancies")
            .queryParam("text", keyword)
            .queryParam("page", page)
            .queryParam("per_page", perPage)
            .build()
            .toUri()

        return restTemplate.getForObject(uri) ?: HhVacanciesResponse(emptyList(), pages = 0)
    }
}
