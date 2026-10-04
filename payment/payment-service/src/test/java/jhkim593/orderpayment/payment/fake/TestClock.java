package jhkim593.orderpayment.payment.fake;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class TestClock extends Clock {
    private Instant now = Instant.now();
    private final ZoneId zone = ZoneId.systemDefault();

    public void advanceSeconds(long seconds) {
        now = now.plus(Duration.ofSeconds(seconds));
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Instant instant() {
        return now;
    }
}
