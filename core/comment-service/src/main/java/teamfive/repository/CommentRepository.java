package teamfive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import teamfive.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEventIdOrderByIdDesc(Long eventId);

    List<Comment> findAllByUserIdOrderByIdDesc(Long userId);
}
