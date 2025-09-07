package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.InquiryState
import com.kmouit.capstone.domain.jpa.InquiryItem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InquiryItemRepository :JpaRepository<InquiryItem, Long>{
    fun findByInquiryState(state: InquiryState, pageable: Pageable): Page<InquiryItem>
}