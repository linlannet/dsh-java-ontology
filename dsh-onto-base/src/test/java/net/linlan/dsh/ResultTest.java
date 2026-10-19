package net.linlan.dsh.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void success_noData() {
        Result<Void> result = Result.success();
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void success_withData() {
        Result<String> result = Result.success("hello");
        assertEquals(200, result.getCode());
        assertEquals("hello", result.getData());
    }

    @Test
    void fail_defaultCode() {
        Result<Void> result = Result.fail("error");
        assertEquals(500, result.getCode());
        assertEquals("error", result.getMsg());
    }

    @Test
    void fail_customCode() {
        Result<Void> result = Result.fail(403, "forbidden");
        assertEquals(403, result.getCode());
        assertEquals("forbidden", result.getMsg());
    }

    @Test
    void unauthorized() {
        Result<Void> result = Result.unauthorized("no token");
        assertEquals(401, result.getCode());
    }

    @Test
    void notFound() {
        Result<Void> result = Result.notFound("not found");
        assertEquals(404, result.getCode());
    }

    @Test
    void badRequest() {
        Result<Void> result = Result.badRequest("bad request");
        assertEquals(400, result.getCode());
    }
}
