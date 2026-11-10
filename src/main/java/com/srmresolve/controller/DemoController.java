package com.srmresolve.controller;

import com.srmresolve.dao.UserDao;
import com.srmresolve.model.User;
import com.srmresolve.service.VivaSimulationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Optional;

@Controller
@RequestMapping("/demo")
public class DemoController {

    private final UserDao userDao;
    private final VivaSimulationService simulationService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public DemoController(UserDao userDao, VivaSimulationService simulationService) {
        this.userDao = userDao;
        this.simulationService = simulationService;
    }

    @GetMapping("/switch-role")
    public String switchRole(@RequestParam("role") String targetRole,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        if (!simulationService.isDemoModeEnabled()) {
            return "redirect:/auth/login";
        }

        String targetEmail;
        String redirectUrl;

        switch (targetRole.toUpperCase()) {
            case "STUDENT" -> {
                targetEmail = "suraj_sharma@srmap.edu.in";
                redirectUrl = "/student/dashboard";
            }
            case "STAFF" -> {
                targetEmail = "water.staff1@srmap.edu.in";
                redirectUrl = "/department/hub";
            }
            case "HEAD" -> {
                targetEmail = "water.head@srmap.edu.in";
                redirectUrl = "/department/head";
            }
            case "EXECUTIVE", "VC" -> {
                targetEmail = "vc@srmap.edu.in";
                redirectUrl = "/executive/dashboard";
            }
            case "ADMIN" -> {
                targetEmail = "admin@srmap.edu.in";
                redirectUrl = "/executive/dashboard";
            }
            default -> {
                return "redirect:/auth/login";
            }
        }

        Optional<User> userOpt = userDao.findByEmail(targetEmail);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    user.getPasswordHash(),
                    Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
            );
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
        }

        return "redirect:" + redirectUrl;
    }
}

