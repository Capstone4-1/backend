package com.kmouit.capstone.service

import com.kmouit.capstone.BoardType
import com.kmouit.capstone.repository.jpa.PostRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class CrawlLogService(
    private val postRepository: PostRepository
) {
    /**
     * 마지막 크롤링 날짜 반환
     */
    fun getLastCrawledTime(targetName: String): LocalDateTime {
        val boardType = BoardType.from(targetName)!!
        val latestDate = postRepository.findTopByBoardTypeOrderByCreatedDateDesc(boardType)?.createdDate
        return latestDate ?: LocalDateTime.of(2025, 3, 1, 0, 0)
    }
}
