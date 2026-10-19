package net.linlan.dsh.common;

import java.io.Serializable;

public class PageRequest implements Serializable {

    private int pageNum = 1;
    private int pageSize = 10;
    private String orderBy;
    private String orderDir = "asc";

    public int getPageNum() {
        return pageNum < 1 ? 1 : pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize < 1 ? 10 : Math.min(pageSize, 100);
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderDir() {
        return "desc".equalsIgnoreCase(orderDir) ? "desc" : "asc";
    }

    public void setOrderDir(String orderDir) {
        this.orderDir = orderDir;
    }

    public int getOffset() {
        return (getPageNum() - 1) * getPageSize();
    }
}
