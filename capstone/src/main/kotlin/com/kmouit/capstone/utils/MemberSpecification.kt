import com.kmouit.capstone.domain.Member
import com.kmouit.capstone.Role
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.SetJoin
import org.springframework.data.jpa.domain.Specification

object MemberSpecification {

    fun search(username: String?, name: String?, role: String?): Specification<Member> {
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            username?.let {
                predicates.add(cb.equal(root.get<String>("username"), it))
            }

            name?.let {
                predicates.add(cb.like(root.get("name"), "%$it%"))
            }

            role?.let {
                val join: SetJoin<Member, Role> = root.joinSet("roles")
                predicates.add(cb.equal(join, Role.from(it)))
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}
