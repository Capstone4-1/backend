package com.kmouit.capstone.service

import com.kmouit.capstone.api.BannerListResponse
import com.kmouit.capstone.api.BannerRequest
import com.kmouit.capstone.api.BannerResponse
import com.kmouit.capstone.domain.jpa.Banner
import com.kmouit.capstone.domain.jpa.BannerType
import com.kmouit.capstone.repository.jpa.BannerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class BannerService(
    private val bannerRepository: BannerRepository,
) {
    @Transactional
    fun saveAll(banners: List<BannerRequest>){
        // 완전 덮어쓰기 → 기존 데이터 전체 삭제 후 저장
        bannerRepository.deleteAll()

        val saved = bannerRepository.saveAll(
            banners.map { req ->
                Banner(
                    title = req.title,
                    targetUrl = req.targetUrl,
                    bannerType = req.bannerType,
                    displayOrder = req.displayOrder,
                    content = req.content
                )
            }
        )
         saved.map { BannerResponse.from(it) }
    }

    fun getBanners(): BannerListResponse {
        val allBanners: List<Banner> = bannerRepository.findAll()

        val normalBanners = allBanners
            .filter { it.bannerType == BannerType.NORMAL }
            .sortedBy { it.displayOrder }
            .map { BannerResponse.from(it) }

        val mediaBanners = allBanners
            .filter { it.bannerType == BannerType.MEDIA }
            .sortedBy { it.displayOrder }
            .map { BannerResponse.from(it) }

        return BannerListResponse(
            normalBanners = normalBanners,
            mediaBanners = mediaBanners
        )
    }


    @Transactional
    fun deleteOne(id: Long) {
        val banner = bannerRepository.findById(id)
            .orElseThrow { RuntimeException("배너를 찾을 수 없습니다: $id") }
        bannerRepository.delete(banner)
    }


}