package entity;

import annotations.Entity;

@Entity("Member")
public class Member extends BaseEntity {
	private Integer id;
	private String name;
	private byte[] passwd;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getPasswd() {
		return passwd;
	}

	public void setPasswd(byte[] passwd) {
		this.passwd = passwd;
	}

}
