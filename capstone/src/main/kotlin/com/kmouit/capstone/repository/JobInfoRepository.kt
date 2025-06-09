package com.kmouit.capstone.repository

import com.kmouit.capstone.domain.JobInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface JobInfoRepository :JpaRepository<JobInfo, Long>{
}