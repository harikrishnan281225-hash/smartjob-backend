package com.smartjob.security;

import com.smartjob.entity.User;
import com.smartjob.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userService.processOAuth2User(email, name);
        String mode = request.getParameter("mode");

        if ("register".equals(mode)) {
            getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/login?registered=true");
        } else {
            String jwt = jwtUtils.generateToken(user.getUsername());

            // DEFENSIVE FIX: Ensure role is never null
            String role = user.getRole();
            if (role == null || role.isEmpty()) {
                role = "USER";
            }

            // Build the URL safely
            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/redirect")
                    .queryParam("token", jwt)
                    .queryParam("role", role)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }
}