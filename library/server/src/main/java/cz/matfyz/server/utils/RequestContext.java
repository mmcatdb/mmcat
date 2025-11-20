package cz.matfyz.server.utils;

import cz.matfyz.server.exception.SessionException;
import cz.matfyz.server.utils.entity.Id;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class RequestContext {

    private @Nullable Id sessionId;

    public void setup(Id sessionId) {
        this.sessionId = sessionId;
    }

    public Id getSessionId() {
        if (sessionId == null)
            // TODO create new session instead.
            // In the current implementation, this class isn't accessible from @Scheduled tasks because of the @RequestScope. We should fix this by creating a more universal context.
            // https://medium.com/soobr/harnessing-the-power-of-request-scoped-beans-requestscope-in-a-non-web-based-request-dc09b1b46373
            throw SessionException.notSet();

        return sessionId;
    }

}
