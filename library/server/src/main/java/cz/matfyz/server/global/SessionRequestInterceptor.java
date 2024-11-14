package cz.matfyz.server.global;

import cz.matfyz.server.entity.Id;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

@Component
public class SessionRequestInterceptor implements HandlerInterceptor {

    @Autowired
    private RequestContext requestContext;

    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        final Cookie cookie = WebUtils.getCookie(request, "session");
        if (cookie == null)
            return true;

        requestContext.setup(new Id(cookie.getValue()));

        return true;
    }

}
