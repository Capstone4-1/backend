package com.kmouit.capstone.repository.jpa

import com.kmouit.capstone.domain.jpa.RoleRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface RoleRequestRepository :JpaRepository<RoleRequest, Long>{
}