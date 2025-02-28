package com.myPrivateNote.configurations;

import com.myPrivateNote.models.User;
import com.myPrivateNote.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@RequiredArgsConstructor
public class UserInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
                    && !authentication.getPrincipal().equals("anonymousUser");

            String userName = null;
            String imgUrl = null;
            boolean isGoogleUser = false;

            if (isAuthenticated) {
                Object principal = authentication.getPrincipal();

                // Si connexion via Google OAuth2
                if (principal instanceof OAuth2User oauth2User) {
                    userName = oauth2User.getAttribute("name");
                    imgUrl = oauth2User.getAttribute("picture");
                    isGoogleUser = true;
                }

                // Si connexion via un compte classique
                if (principal instanceof UserDetails userDetails) {
                    userName = userDetails.getUsername();
                    User userLogin = userRepository.findByUsername(userName);
                    if (userLogin != null) {
                        imgUrl = userLogin.getImgUser_url();
                    }
                }
            }

            // Ajout des attributs au modèle
            modelAndView.addObject("isAuthenticated", isAuthenticated);
            modelAndView.addObject("isGoogleUser", isGoogleUser);
            modelAndView.addObject("userName", userName);
            modelAndView.addObject("picture", imgUrl);
        }
    }
}
