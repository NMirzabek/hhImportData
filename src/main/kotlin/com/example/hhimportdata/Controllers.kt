package com.example.hhimportdata

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.context.SecurityContextImpl


@RestController
@RequestMapping("/api/vacancies")
class VacancyController(
    private val vacancyService: VacancyService
) {

    data class FetchResponse(val message: String)

    @PostMapping("/fetch")
    fun fetch(
        @RequestParam keyword: String,
        @RequestParam(required = false, defaultValue = "1") pages: Int
    ): FetchResponse {
        val saved = vacancyService.fetchAndStore(keyword, pagesToLoad = pages)
        return FetchResponse("Fetched and stored $saved vacancies for keyword '$keyword'")
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): VacancyDto =
        vacancyService.getById(id)

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


@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val telegramAuthService: TelegramAuthService,
    private val userService: UserService
) {

    @GetMapping("/telegram")
    fun telegramCallback(
        @RequestParam allParams: Map<String, String>,
        request: HttpServletRequest
    ): ResponseEntity<String> {

        if (!telegramAuthService.verifyTelegramData(allParams)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid Telegram auth data")
        }

        val telegramId = allParams["id"] ?: return ResponseEntity.badRequest().body("Missing id")
        val username = allParams["username"]
        val firstName = allParams["first_name"]
        val lastName = allParams["last_name"]

        val user = userService.findOrCreateFromTelegram(
            telegramId = telegramId,
            username = username,
            firstName = firstName,
            lastName = lastName
        )

        val auth = UsernamePasswordAuthenticationToken(
            user,
            null,
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )

        val context = SecurityContextImpl()
        context.authentication = auth

        SecurityContextHolder.setContext(context)

        val session = request.getSession(true)
        session.setAttribute("SPRING_SECURITY_CONTEXT", context)

        return ResponseEntity.ok("Telegram authorization successful. You can close this tab.")
    }

    @GetMapping("/me")
    fun me(): ResponseEntity<UserDto> {
        val auth = SecurityContextHolder.getContext().authentication
        val user = auth?.principal as? User
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(user.toDto())
    }
}
