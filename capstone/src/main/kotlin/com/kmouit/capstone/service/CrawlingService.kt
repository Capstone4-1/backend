package com.kmouit.capstone.service

import com.kmouit.capstone.api.CrawledNoticeDto
import com.kmouit.capstone.domain.CrawledNotice
import com.kmouit.capstone.repository.CrawledNoticeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(readOnly = true)
class CrawlingService(
    private val crawledNoticeRepository: CrawledNoticeRepository
) {

    @Transactional
    fun saveCrawledNotices(noticeList: List<CrawledNoticeDto>) {
        for (crawledNoticeDto in noticeList) {
            val newData = CrawledNotice()
            newData.title = crawledNoticeDto.title
            newData.content = crawledNoticeDto.content
            newData.date = crawledNoticeDto.date
            newData.imageUrls = crawledNoticeDto.url
            crawledNoticeRepository.save(newData)
        }
    }

}
