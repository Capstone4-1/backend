package com.kmouit.capstone.api

import com.kmouit.capstone.jwt.CustomUserDetails
import com.kmouit.capstone.service.PostService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


@PreAuthorize("permitAll()")
@RestController
@RequestMapping("api/board")
@Controller
class BoardController(
    private val postService: PostService

) {
    @GetMapping("/favorites")
    fun getMyFavorites(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
    ): ResponseEntity<Map<String, Any>> {
        val result = postService.findMyFavorites(userDetails.getId())
        return ResponseEntity.ok(
            mapOf(
                "message" to "my 즐겨찾기 조회 성공",
                "favorites" to result
            )
        )
    }

    @PostMapping("/favorites")
    fun responseSaveBoardMark(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestBody request: FavoriteRequest,
    ): ResponseEntity<Map<String, String>> {
        postService.saveBoardMarkInfo(userDetails.getId(), request.boardType)
        return ResponseEntity.ok(
            mapOf(
                "message" to "my 즐겨찾기 추가 성공",
            )
        )
    }


    @DeleteMapping("/favorites")
    fun responseDeleteBoardMark(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam boardType: String,
    ): ResponseEntity<Map<String, String>> {
        postService.deleteBoardMarkInfo(userDetails.getId(), boardType)
        return ResponseEntity.ok(mapOf("message" to "즐겨찾기 삭제 성공"))
    }
}