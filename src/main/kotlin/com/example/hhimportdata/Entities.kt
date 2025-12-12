package com.example.hhimportdata

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime
import java.util.Date

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    var createdDate: Date? = null,

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    var modifiedDate: Date? = null,

    @CreatedBy
    var createdBy: Long? = null,

    @LastModifiedBy
    var lastModifiedBy: Long? = null,

    @Column(nullable = false)
    @ColumnDefault("false")
    var deleted: Boolean = false
)


@Entity
@Table(name = "vacancy")
class Vacancy(

    @Column(name = "hh_id", nullable = false, unique = true)
    var hhId: String,

    @Column(nullable = false)
    var title: String,

    var employerName: String? = null,
    var areaName: String? = null,
    var addressCity: String? = null,

    var salaryFrom: Long? = null,
    var salaryTo: Long? = null,
    var currency: String? = null,

    @Enumerated(EnumType.STRING)
    var workFormat: WorkFormat? = null,

    var employmentType: String? = null,
    var experienceLevel: String? = null,

    @Column(columnDefinition = "text")
    var requirement: String? = null,

    @Column(columnDefinition = "text")
    var responsibility: String? = null,

    var createdAtHh: OffsetDateTime? = null,
    var publishedAtHh: OffsetDateTime? = null,

    var webUrl: String? = null

) : BaseEntity()
