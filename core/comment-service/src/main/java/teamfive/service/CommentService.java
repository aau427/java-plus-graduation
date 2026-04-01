package teamfive.service;

import teamfive.dto.comment.CommentDto;
import teamfive.dto.comment.InputCommentDto;
import teamfive.dto.comment.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto create(Long userId, InputCommentDto commentDto);

    void deleteByIdByAdmin(Long id);

    void deleteForOwner(Long userId, Long commentId);

    List<CommentDto> getByEventId(Long eventId);

    List<CommentDto> getAllForUser(Long userId);

    CommentDto updateComment(Long commentId, Long userId, UpdateCommentDto dto);
}
