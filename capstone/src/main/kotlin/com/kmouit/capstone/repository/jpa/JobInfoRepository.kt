package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.JobInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface JobInfoRepository :JpaRepository<JobInfo, Long>{
}