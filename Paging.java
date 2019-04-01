package com.yy.yycloud.commons;

public class Paging {

    //region member fields
    private int size;
    private int current;
    private int total;
    private long recordsTotal;
    //endregion

    public Paging() {
        this.current = 1;
        this.size = 10;
    }

    public Paging(int pageSize, int current) {
        this.size = pageSize;
        this.current = current;
    }

    //region getter setter
    // 名称冗余是因为在页面中绑定到 controller 时，防止与业务字段冲突
    public int getCurrent() {
        return current;
    }

    public void setPagingCurrent(int current) {
        if (current > 0) {
            this.current = current;
        }
        else {
            this.current = 1;
        }
    }

    public int getPageSize() {
        return this.size;
    }

    public void setPagingPageSize(int size) {
        this.size = size;
    }

    // 页数
    public int getTotal() {
        return total;
    }

    // 记录数
    public long getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(long recordsTotal) {
        this.recordsTotal = recordsTotal;
        if (recordsTotal <= 0) return;
        this.total = (int)((recordsTotal - 1) / this.size + 1);
    }
    //endregion
}
