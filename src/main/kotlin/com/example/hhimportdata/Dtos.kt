package com.example.hhimportdata

import com.fasterxml.jackson.annotation.JsonProperty


data class VacancyDto(
    val id: Long,
    val hhId: String,
    val title: String,
    val employerName: String?,
    val areaName: String?,
    val addressCity: String?,
    val salaryFrom: Long?,
    val salaryTo: Long?,
    val currency: String?,
    val workFormat: WorkFormat?,
    val webUrl: String?
)

data class VacancySearchFilter(
    val keyword: String?,
    val minSalary: Long?,
    val maxSalary: Long?,
    val city: String?,
    val workFormat: WorkFormat?
)

fun Vacancy.toDto() = VacancyDto(
    id = requireNotNull(this.id),
    hhId = this.hhId,
    title = this.title,
    employerName = this.employerName,
    areaName = this.areaName,
    addressCity = this.addressCity,
    salaryFrom = this.salaryFrom,
    salaryTo = this.salaryTo,
    currency = this.currency,
    workFormat = this.workFormat,
    webUrl = this.webUrl
)


data class UserDto(
    val id: Long,
    val telegramId: String,
    val username: String?,
    val firstName: String?,
    val lastName: String?
)

fun User.toDto() = UserDto(
    id = requireNotNull(this.id),
    telegramId = this.telegramId,
    username = this.username,
    firstName = this.firstName,
    lastName = this.lastName
)


data class HhVacanciesResponse(
    val items: List<HhVacancyItem>,
    val pages: Int
)

data class HhVacancyItem(
    val id: String,
    val name: String,
    val area: HhArea?,
    val salary: HhSalary?,
    val address: HhAddress?,

    @JsonProperty("work_format")
    val workFormat: List<HhWorkFormat>?,

    val employment: HhSimpleRef?,
    val experience: HhSimpleRef?,
    val employer: HhEmployer?,
    val snippet: HhSnippet?,

    @JsonProperty("created_at")
    val createdAt: String?,

    @JsonProperty("published_at")
    val publishedAt: String?,

    @JsonProperty("alternate_url")
    val alternateUrl: String?
)

data class HhArea(
    val id: String?,
    val name: String?
)

data class HhSalary(
    @JsonProperty("from")
    val from: Long?,
    @JsonProperty("to")
    val to: Long?,
    val currency: String?
)

data class HhAddress(
    val city: String?
)

data class HhWorkFormat(
    val id: String?,
    val name: String?
)

data class HhSimpleRef(
    val id: String?,
    val name: String?
)

data class HhEmployer(
    val id: String?,
    val name: String?
)

data class HhSnippet(
    val requirement: String?,
    val responsibility: String?
)
