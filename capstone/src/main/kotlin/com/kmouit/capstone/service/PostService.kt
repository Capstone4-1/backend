package com.kmouit.capstone.service
import com.kmouit.capstone.BoardType
import com.kmouit.capstone.api.PostRequestDto
import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.domain.PostDto
import com.kmouit.capstone.domain.Posts
import com.kmouit.capstone.domain.toDto
import com.kmouit.capstone.repository.MemberRepository
import com.kmouit.capstone.repository.PostRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.NoSuchElementException
import kotlin.jvm.optionals.getOrNull


@Service
@Transactional(readOnly = true)
class PostService(
    private val memberRepository: MemberRepository,
    private val postRepository: PostRepository
) {

    @Transactional
    fun createPost(requestDto: PostRequestDto, member: Member) {
        val createBy = memberRepository.findById(member.id!!).getOrNull()
            ?: throw NoSuchElementException("멤버를 찾을 수 없습니다.")
        val newPost = Posts()
        newPost.createdDate = LocalDateTime.now()
        newPost.member = createBy
        newPost.boardType = BoardType.from(requestDto.boardType)
        newPost.title = requestDto.title
        newPost.content = requestDto.content

        postRepository.save(newPost)
    }


    fun findAllByBoardType(boardType: String, pageable: Pageable): Page<PostDto> {
        val postPage = postRepository.findAllByBoardTypeOrderByCreatedDateDesc(
            BoardType.from(boardType)!!, pageable
        )
        return postPage.map { it.toDto() }
    }
}

