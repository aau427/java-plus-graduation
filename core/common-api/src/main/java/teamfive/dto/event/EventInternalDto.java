package teamfive.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import teamfive.enums.EventState;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventInternalDto {

    private Long id;
    private EventState state;
    private Long initiatorId;
    private Integer confirmedRequests;
    private Integer participantLimit;
    private Boolean requestModeration;
}
