package net.linlan.dsh.sys.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import net.linlan.dsh.common.TenantContext;
import net.linlan.dsh.sys.entity.SysAuditLog;
import net.linlan.dsh.sys.security.AuditLog;
import net.linlan.dsh.sys.service.SysAuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 审计日志切面，拦截标注 @AuditLog 的方法，记录操作日志
 * @author Linlan
 * CreatedAt 2026-10-18
 * @version 1.0
 * @since 0.2.1
 */
@Aspect
@Component
public class AuditLogAspect {

    private final SysAuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public AuditLogAspect(SysAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
        this.objectMapper = new ObjectMapper();
    }

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        String resultStatus = "SUCCESS";
        String errorMsg = null;
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            resultStatus = "FAILED";
            errorMsg = ex.getMessage();
            throw ex;
        } finally {
            long executionMs = System.currentTimeMillis() - startTime;
            try {
                recordAuditLog(joinPoint, auditLog, resultStatus, errorMsg, executionMs);
            } catch (Exception ignored) {
            }
        }
    }

    private void recordAuditLog(ProceedingJoinPoint joinPoint, AuditLog auditLog,
                                 String resultStatus, String errorMsg, long executionMs) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        SysAuditLog logEntry = new SysAuditLog();
        logEntry.setUserId(TenantContext.getUserId());
        logEntry.setTenantId(TenantContext.getTenantId());
        logEntry.setRoleLevel(TenantContext.getRoleLevel());
        logEntry.setAction(auditLog.action());
        logEntry.setTargetType(auditLog.targetType());
        logEntry.setMethod(className + "." + methodName);
        logEntry.setResultStatus(resultStatus);
        logEntry.setErrorMsg(errorMsg);
        logEntry.setExecutionMs(executionMs);

        try {
            String params = objectMapper.writeValueAsString(joinPoint.getArgs());
            logEntry.setRequestParams(params.length() > 2000 ? params.substring(0, 2000) : params);
        } catch (Exception e) {
            logEntry.setRequestParams(Arrays.toString(joinPoint.getArgs()));
        }

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            logEntry.setRequestUri(request.getRequestURI());
            logEntry.setClientIp(request.getRemoteAddr());
        }

        auditLogService.asyncSave(logEntry);
    }
}
