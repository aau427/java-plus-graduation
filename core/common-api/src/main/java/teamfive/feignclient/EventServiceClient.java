package teamfive.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import teamfive.dto.event.EventInternalDto;

//TODO: в конце main-service поменяется на event-service, как сказал наставник
@FeignClient(name = "main-service", path = "/internal/events")
public interface EventServiceClient {

    @PutMapping("/{eventId}/confirmed")
    void incrementConfirmedRequests(
            @PathVariable("eventId") Long eventId,
            @RequestParam("count") Integer count
    );

    @GetMapping("/{eventId}")
    EventInternalDto getEventInternalById(@PathVariable Long eventId);
}
