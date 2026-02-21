package net.arctel.framework.utils;

import lombok.Data;
  
import java.util.*;  
  
public class CustomPaginationUtil {  
      
    /**  
     * 分页结果封装类  
     *  
     * @param <T> 数据类型  
     */  
    @Data  
    public static class PageResult<T> {  
        private List<T> rows;  
        private int current;   // 当前页码（从1开始）  
        private int total;     // 总记录数  
        private int totalPage; // 总页数  
  
        public PageResult(List<T> rows, int current, int total, int totalPage) {  
            this.rows = rows != null ? new ArrayList<>(rows) : Collections.emptyList();  
            this.current = current;  
            this.total = total;  
            this.totalPage = totalPage;  
        }  
  
        @Override  
        public String toString() {  
            return "PageResult{" +  
                    "rows=" + rows +  
                    ", current=" + current +  
                    ", total=" + total +  
                    ", totalPage=" + totalPage +  
                    '}';  
        }  
    }  
  
    /**  
     * 自定义分页方法，返回包含 rows、current、total 和 totalPage 的结构  
     *  
     * @param list       原始数据列表  
     * @param pageNo     页码（从1开始）  
     * @param pageSize   每页大小  
     * @param comparator 排序比较器，可为 null  
     * @param <T>        列表元素类型  
     * @return 分页结果对象  
     */  
    public static <T> PageResult<T> pagination(  
            List<T> list, int pageNo, int pageSize, Comparator<T> comparator) {  
  
        if (list == null) {  
            // total=0, totalPage=0  
            return new PageResult<>(Collections.emptyList(), pageNo, 0, 0);  
        }  
  
        int total = list.size();  
  
        // 校验分页参数是否合法  
        if (pageNo <= 0 || pageSize <= 0) {  
            // 参数非法：返回全部数据，视为第1页，总页数为1  
            List<T> allData = new ArrayList<>(list);  
            if (comparator != null) {  
                allData.sort(comparator);  
            }  
            return new PageResult<>(allData, pageNo, total, 1);  
        }  
  
        // 排序  
        List<T> sortedList = new ArrayList<>(list);  
        if (comparator != null) {  
            sortedList.sort(comparator);  
        }  
  
        // 计算总页数（向上取整）  
        int totalPage = (total + pageSize - 1) / pageSize;  
  
        int fromIndex = (pageNo - 1) * pageSize;  
  
        if (fromIndex >= total) {  
            // 超出范围，返回空数据，但保留分页信息  
            return new PageResult<>(Collections.emptyList(), pageNo, total, totalPage);  
        }  
  
        int toIndex = Math.min(fromIndex + pageSize, total);  
        List<T> pageData = new ArrayList<>(sortedList.subList(fromIndex, toIndex));  
        return new PageResult<>(pageData, pageNo, total, totalPage);  
    }  
}