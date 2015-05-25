package com.joymove.test;

public class testEhcacheEntity {
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public testEhcacheEntity(int id) {
		super();
		this.id = id;
	}

	public testEhcacheEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		testEhcacheEntity other = (testEhcacheEntity) obj;
		if (id != other.id)
			return false;
		return true;
	}

	
	
}
