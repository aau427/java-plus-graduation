package teamfive.service;

import ru.practicum.ewm.stats.proto.UserActionProto;

public interface CollectorService {
    void processUserAction(UserActionProto request);
}
