package teamfive.event.view;

import teamfive.enums.EventState;

public interface EventInternalView {
    Long getId();

    EventState getState();

    Long getInitiatorId(); //

    Integer getConfirmedRequests();

    Integer getParticipantLimit();

    Boolean getRequestModeration();
}
