package teamfive.client.statclient;

//TODO: подлежит рефакторингу, т.к. больше нет stats-server
public class StatsServerUnavailable extends RuntimeException {
    public StatsServerUnavailable(String message, Throwable cause) {
        super(message, cause);
    }
}
