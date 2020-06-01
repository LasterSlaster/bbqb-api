package de.bbqb.backend.api.model.entity;

public class User {

	private Long id;
	private String name;
	//private Group group;
	//private Role role;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
}
