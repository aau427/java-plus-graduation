package teamfive.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamfive.dto.comment.CommentDto;
import teamfive.dto.comment.InputCommentDto;
import teamfive.dto.comment.UpdateCommentDto;
import teamfive.dto.event.EventInternalDto;
import teamfive.dto.user.UserDto;
import teamfive.enums.EventState;
import teamfive.exception.ConflictException;
import teamfive.exception.NotFoundException;
import teamfive.feignclient.EventServiceClient;
import teamfive.feignclient.UserServiceClient;
import teamfive.mapper.CommentMapper;
import teamfive.model.Comment;
import teamfive.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository repository;
    private final UserServiceClient userClient;
    private final CommentMapper mapper;
    private final EventServiceClient eventClient;

    @Override
    @Transactional
    public CommentDto create(Long userId, InputCommentDto commentDto) {
        UserDto user = getUserDtoOrTrow(userId);

        EventInternalDto event = eventClient.getEventInternalById(commentDto.getEventId());

        validateEventState(event);

        Comment comment = buildComment(user, event, commentDto.getText());
        log.debug("Комментарий перед сохранением: {}", comment);

        return mapper.toCommentDto(repository.save(comment));
    }

    @Override
    @Transactional
    public void deleteByIdByAdmin(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Комментарий с id={" + id + "} не найден");
        }
        repository.deleteById(id);
        log.debug("Комментарий с id={} удален администратором", id);
    }

    @Override
    @Transactional
    public void deleteForOwner(Long userId, Long commentId) {
        Comment comment = findCommentById(commentId);

        if (!comment.getUserId().equals(userId)) {
            throw new ConflictException("Удаление невозможно. Обратитесь к администратору или владельцу");
        }

        repository.deleteById(commentId);
        log.debug("Комментарий с id={} удален владельцем", commentId);
    }

    @Override
    public List<CommentDto> getByEventId(Long eventId) {
        EventInternalDto eventDto = eventClient.getEventInternalById(eventId);
        return repository.findAllByEventIdOrderByIdDesc(eventId)
                .stream()
                .map(mapper::toCommentDto)
                .toList();
    }

    @Override
    public List<CommentDto> getAllForUser(Long userId) {
        UserDto user = getUserDtoOrTrow(userId);

        return repository.findAllByUserIdOrderByIdDesc(userId)
                .stream()
                .map(mapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long commentId, Long userId, UpdateCommentDto dto) {
        Comment existingComment = findCommentById(commentId);
        validateCommentOwnership(userId, existingComment);

        if (dto.getText() != null) {
            existingComment.setText(dto.getText());
            repository.save(existingComment);
        }
        return mapper.toCommentDto(existingComment);
    }

    private void validateEventState(EventInternalDto event) {
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Событие не опубликовано. Комментарии запрещены");
        }
    }

    private Comment buildComment(UserDto user, EventInternalDto event, String text) {
        return Comment.builder()
                .userId(user.getId())
                .eventId(event.getId())
                .text(text)
                .created(LocalDateTime.now())
                .build();
    }

    private Comment findCommentById(Long commentId) {
        return repository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id={" + commentId + "} не найден"));
    }

    private void validateCommentOwnership(Long userId, Comment comment) {
        if (!comment.getUserId().equals(userId)) {
            throw new ConflictException("Обновлять комментарий может только автор!");
        }
    }

    private UserDto getUserDtoOrTrow(Long userId) {
        return userClient.getByIds(List.of(userId))
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    }
}