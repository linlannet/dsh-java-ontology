package net.linlan.dsh.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResultTest {

    @Test
    void empty_pageResult() {
        PageResult<String> result = PageResult.empty(1, 10);
        assertTrue(result.getRecords().isEmpty());
        assertEquals(0, result.getTotal());
        assertEquals(1, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(0, result.getPages());
    }

    @Test
    void populated_pageResult() {
        List<String> records = List.of("a", "b", "c");
        PageResult<String> result = new PageResult<>(records, 25, 2, 10);
        assertEquals(3, result.getRecords().size());
        assertEquals(25, result.getTotal());
        assertEquals(2, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(3, result.getPages());
    }

    @Test
    void nullRecords_treatedAsEmpty() {
        PageResult<String> result = new PageResult<>(null, 0, 1, 10);
        assertNotNull(result.getRecords());
        assertTrue(result.getRecords().isEmpty());
    }
}
