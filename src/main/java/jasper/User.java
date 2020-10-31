package jasper;

import java.util.List;

public class User {
	private List<City> cities;
	public List<City> getCities() {
		return cities;
	}
	public void setCities(List<City> cities) {
		this.cities = cities;
	}
	private int id;
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
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	private String name;
	private int age;
	
	public User() {
	}
	public User(List<City> cities, int id, String name, int age) {
		super();
		this.cities = cities;
		this.id = id;
		this.name = name;
		this.age = age;
	}
	
	
}
