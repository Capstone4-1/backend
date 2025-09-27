package com.kmouit.capstone.domain.jpa

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id


@Entity
class Banner(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var title: String? = null,

    var content :String? = null,

    var targetUrl: String? = null,

    var bannerType: BannerType? = null,

    var displayOrder : Int? = null //순서
    ) {
}

enum class BannerType {
    NORMAL, MEDIA
}