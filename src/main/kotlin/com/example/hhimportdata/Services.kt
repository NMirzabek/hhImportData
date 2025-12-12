package com.example.hhimportdata

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


@Service
class VacancyService(
    private val hhClient: HhClient,
    private val vacancyRepository: VacancyRepository
) {

    private val hhDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxx")

    private fun parseHhDateTime(value: String?): OffsetDateTime? {
        if (value == null) return null
        return try {
            OffsetDateTime.parse(value, hhDateFormatter)
        } catch (ex: DateTimeParseException) {
            null
        }
    }

    @Transactional
    fun fetchAndStore(keyword: String, pagesToLoad: Int = 1, perPage: Int = 20): Int {
        val allNewEntities = mutableListOf<Vacancy>()
        val seenHhIds = mutableSetOf<String>()

        for (page in 0 until pagesToLoad) {
            val response = hhClient.searchVacancies(keyword, page, perPage)

            val pageItems = response.items
            val pageHhIds = pageItems.map { it.id }

            val existingHhIds = vacancyRepository
                .findAllByHhIdIn(pageHhIds)
                .map { it.hhId }
                .toSet()

            val newEntities = pageItems
                .filter { it.id !in existingHhIds }
                .filter { seenHhIds.add(it.id) }
                .map { item -> toEntity(item) }

            allNewEntities += newEntities

            if (page >= response.pages - 1) break
        }

        if (allNewEntities.isEmpty()) {
            return 0
        }

        val saved = vacancyRepository.saveAll(allNewEntities)
        return saved.size
    }

    private fun toEntity(item: HhVacancyItem): Vacancy {
        val workFormatIds = item.workFormat?.mapNotNull { it.id } ?: emptyList()
        val workFormat = when {
            "REMOTE" in workFormatIds && "ON_SITE" in workFormatIds -> WorkFormat.HYBRID
            "REMOTE" in workFormatIds -> WorkFormat.REMOTE
            "ON_SITE" in workFormatIds -> WorkFormat.OFFICE
            else -> null
        }

        return Vacancy(
            hhId = item.id,
            title = item.name,
            employerName = item.employer?.name,
            areaName = item.area?.name,
            addressCity = item.address?.city,
            salaryFrom = item.salary?.from,
            salaryTo = item.salary?.to,
            currency = item.salary?.currency,
            workFormat = workFormat,
            employmentType = item.employment?.id ?: item.employment?.name,
            experienceLevel = item.experience?.id ?: item.experience?.name,
            requirement = item.snippet?.requirement,
            responsibility = item.snippet?.responsibility,
            createdAtHh = parseHhDateTime(item.createdAt),
            publishedAtHh = parseHhDateTime(item.publishedAt),
            webUrl = item.alternateUrl
        )
    }

    fun getById(id: Long): VacancyDto =
        vacancyRepository.findById(id)
            .map { it.toDto() }
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Vacancy $id not found")
            }

    fun search(filter: VacancySearchFilter): List<VacancyDto> {
        val all = vacancyRepository.findAll()

        return all.asSequence()
            .filter { v ->
                filter.keyword?.takeIf { it.isNotBlank() }?.let { kw ->
                    val k = kw.lowercase()
                    v.title.lowercase().contains(k) ||
                            (v.requirement?.lowercase()?.contains(k) == true) ||
                            (v.responsibility?.lowercase()?.contains(k) == true)
                } ?: true
            }
            .filter { v ->
                filter.minSalary?.let { min -> (v.salaryFrom ?: 0L) >= min } ?: true
            }
            .filter { v ->
                filter.maxSalary?.let { max -> (v.salaryTo ?: Long.MAX_VALUE) <= max } ?: true
            }
            .filter { v ->
                filter.city?.takeIf { it.isNotBlank() }?.let { c ->
                    v.addressCity?.contains(c, ignoreCase = true) == true
                } ?: true
            }
            .filter { v ->
                filter.workFormat?.let { wf -> v.workFormat == wf } ?: true
            }
            .map { it.toDto() }
            .toList()
    }
}


@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun findOrCreateFromTelegram(
        telegramId: String,
        username: String?,
        firstName: String?,
        lastName: String?
    ): User {
        val existing = userRepository.findByTelegramId(telegramId)
        if (existing != null) return existing

        val newUser = User(
            telegramId = telegramId,
            username = username,
            firstName = firstName,
            lastName = lastName
        )
        return userRepository.save(newUser)
    }
}


@Service
class TelegramAuthService(
    @Value("\${telegram.bot-token}")
    private val botToken: String
) {

    fun verifyTelegramData(data: Map<String, String>): Boolean {
        val hash = data["hash"] ?: return false

        val dataCheckString = data.entries
            .filter { it.key != "hash" }
            .sortedBy { it.key }
            .joinToString("\n") { "${it.key}=${it.value}" }

        val secretKey = MessageDigest.getInstance("SHA-256")
            .digest(botToken.toByteArray(Charsets.UTF_8))

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretKey, "HmacSHA256"))
        val calcHash = mac.doFinal(dataCheckString.toByteArray(Charsets.UTF_8))

        val calcHex = calcHash.joinToString("") { "%02x".format(it) }

        return calcHex.equals(hash, ignoreCase = true)
    }
}
