package de.bbqb.backend.api.model.entity;

/**
 * BBQ-Butler business object to hold user information
 * @author laster
 */
public class User {

	private Long id;
	private String name;
	//private Group group;
	//private Role role;
	
	public User(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public Long getId() {
		return id;
	}
}
