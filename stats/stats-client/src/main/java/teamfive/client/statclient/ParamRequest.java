package teamfive.client.statclient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

//TODO: возможно подлежит рефакторингу, т.к. больше нет stats-server
@ToString
@Getter
@AllArgsConstructor
@Builder
public class ParamRequest {
    private LocalDateTime start;
    private LocalDateTime end;
    private List<String> uris;
    private Boolean unique;
}
