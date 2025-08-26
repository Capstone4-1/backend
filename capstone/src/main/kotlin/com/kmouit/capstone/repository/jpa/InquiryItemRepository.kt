package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.InquiryItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InquiryItemRepository :JpaRepository<InquiryItem, Long>{
}