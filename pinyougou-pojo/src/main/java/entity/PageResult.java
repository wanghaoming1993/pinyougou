package entity;

import java.io.Serializable;
import java.util.List;

/**
 * @author Administrator 分页查询结果集
 */
public class PageResult implements Serializable{
	private Long total;
	private List rows;
	
	
	public PageResult(Long total, List rows) {
		super();
		this.total = total;
		this.rows = rows;
	}
	public PageResult() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Long getTotal() {
		return total;
	}
	public void setTotal(Long total) {
		this.total = total;
	}
	public List getRows() {
		return rows;
	}
	public void setRows(List rows) {
		this.rows = rows;
	}
	


}
