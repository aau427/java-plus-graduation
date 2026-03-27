package teamfive.feignclient.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import teamfive.dto.event.EventInternalDto;

@FeignClient(name = "event-service",
        path = "/internal/events",
        configuration = FeignEventClientConfig.class,
        fallbackFactory = EventServiceClientFallbackFactory.class
)
public interface EventServiceClient {

    @PutMapping("/{eventId}/confirmed")
    void incrementConfirmedRequests(
            @PathVariable("eventId") Long eventId,
            @RequestParam("count") Integer count
    );

    @GetMapping("/{eventId}")
    EventInternalDto getEventInternalById(@PathVariable Long eventId);
}
