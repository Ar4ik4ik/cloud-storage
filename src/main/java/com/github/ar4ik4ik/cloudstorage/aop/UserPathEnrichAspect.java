package com.github.ar4ik4ik.cloudstorage.aop;

import com.github.ar4ik4ik.cloudstorage.model.StorageUserDetails;
import com.github.ar4ik4ik.cloudstorage.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class UserPathEnrichAspect {

    @Pointcut("within(com.github.ar4ik4ik.cloudstorage.service.StorageService+) " +
            "&& execution(* * (.., @com.github.ar4ik4ik.cloudstorage.aop.PathEnrich (*), ..))")
    public void pathEnrichAnnotatedParams() {}

    @Around("pathEnrichAnnotatedParams()")
    public Object applyEnrichUserPath(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("UserPathAspect triggered for method: {}", joinPoint.getSignature().toShortString());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof StorageUserDetails userDetails)) {
            log.warn("Attempt to access method with @PathEnrich without authenticated StorageUserDetails. Proceeding without path enrichment.");
            return joinPoint.proceed();
        }

        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Annotation[][] paramAnnotations = signature.getMethod().getParameterAnnotations();
        String userRootDirectory = userDetails.getUserRootDirectory();
        boolean pathWasEnriched = false;
        log.debug("Annotations {}", Arrays.stream(paramAnnotations).flatMap(annotations ->
                Arrays.stream(annotations).map(Annotation::toString)).toList());
        if (args.length == 0) {
            log.debug("No args founded in method {}", signature.getMethod().getName());
            joinPoint.proceed();
        }
        for (int i = 0; i < args.length; i++) {
            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation instanceof PathEnrich) {
                    if (args[i] instanceof String originalPath) {
                        String fullPath = PathUtils.getFullPathFromRootAndDestination(userRootDirectory,
                                originalPath);
                        log.debug("Enriching path from '{}' to '{}'", originalPath, fullPath);
                        args[i] = fullPath;
                        pathWasEnriched = true;
                    } else {
                        log.warn("Parameter annotated with @PathEnrich is not instance of String, type is: {}\n" +
                                        "Next processing is skipped",
                                args[i].getClass().getSimpleName());
                    }
                } else {
                    log.debug("Param {} not instance of @PathEnrich", annotation.getClass().getSimpleName());
                }
            }
        }
        if (!pathWasEnriched) {
            log.debug("No path args were enriched in {}. Using original args", joinPoint.getSignature().toShortString());
        }
        return joinPoint.proceed(args);
    }
}
