package com.shopsmart.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * One place that turns exceptions into friendly pages instead of the ugly
 * Spring "Whitelabel Error Page".
 *
 *   - ResourceNotFoundException  -> a nice 404 page
 *   - AccessDeniedException      -> a 403 "not allowed" page
 *   - BusinessException          -> bounce back with a red flash message
 *   - anything else              -> a generic error page (and we log the details)
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoStaticResource(NoResourceFoundException ex, Model model) {
        model.addAttribute("message", "Page not found.");
        return "error/404";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("message", "You are not allowed to view that page.");
        return "error/403";
    }

    /**
     * Business rule broken (e.g. "not enough stock"). We send the customer back
     * to where they came from with a red flash message, which feels natural.
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, RedirectAttributes ra,
                                 HttpServletRequest request) {
        ra.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @ExceptionHandler(Exception.class)
    public String handleEverythingElse(Exception ex, Model model) {
        // Log the full stack trace for us, but show the customer something calm.
        log.error("Unexpected error", ex);
        model.addAttribute("message", "Something went wrong on our side. Please try again.");
        return "error/500";
    }
}
