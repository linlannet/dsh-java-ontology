package net.linlan.dsh.plat.controller;

import net.linlan.dsh.common.Result;
import net.linlan.dsh.plat.model.ApiException;
import net.linlan.dsh.plat.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> apiException(ApiException exception) {
        return ResponseEntity.status(exception.status()).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .findFirst()
                .orElse("请求参数无效");
        return ResponseEntity.badRequest().body(Result.badRequest(message));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> bindException(BindException exception) {
        String message = exception.getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数绑定失败");
        return ResponseEntity.badRequest().body(Result.badRequest(message));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> missingParam(MissingServletRequestParameterException exception) {
        return ResponseEntity.badRequest().body(Result.badRequest("缺少必需参数: " + exception.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> typeMismatch(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest().body(Result.badRequest("参数类型错误: " + exception.getName()));
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Result<Void>> emptyResult(EmptyResultDataAccessException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.notFound("记录不存在"));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Result<Void>> dataAccess(DataAccessException exception) {
        log.error("数据库访问异常", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.fail("数据库操作失败"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> illegalArg(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Result.badRequest(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> generic(Exception exception) {
        log.error("未处理异常", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.fail("服务器内部错误"));
    }
}
