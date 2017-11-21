package cn.weekdragon.nattable.model;

public class SFile {
	private int id;
	private String name;
	private String size;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public SFile(int id, String name, String size) {
		this.id = id;
		this.name = name;
		this.size = size;
	}
	public SFile() {
	}
	@Override
	public String toString() {
		return "SFile [id=" + id + ", name=" + name + ", size=" + size + "]";
	}
}
