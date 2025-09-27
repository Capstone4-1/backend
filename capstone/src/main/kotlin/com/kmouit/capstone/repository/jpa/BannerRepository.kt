package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.Banner
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface BannerRepository :JpaRepository<Banner, Long> {
}