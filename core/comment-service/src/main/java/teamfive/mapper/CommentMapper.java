package teamfive.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import teamfive.dto.comment.CommentDto;
import teamfive.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mappings({
            @Mapping(source = "userId", target = "userId"),
            @Mapping(source = "eventId", target = "eventId")
    })
    CommentDto toCommentDto(Comment comment);
}

