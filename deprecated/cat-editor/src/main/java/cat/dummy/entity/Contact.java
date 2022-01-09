package cat.dummy.entity;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author pavel.koupil
 */
public class Contact {

	private SimpleStringProperty name;
	private SimpleStringProperty id;
	private SimpleStringProperty number;
//	private SimpleStringProperty value;

	public Contact() {
	}

	public Contact(String id, String number, String name/*, String value*/) {

		this.id = new SimpleStringProperty(id);
		this.number = new SimpleStringProperty(number);
		this.name = new SimpleStringProperty(name);
//		this.value = new SimpleStringProperty(value);
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public String getId() {
		return id.get();
	}

	public void setId(String id) {
		this.id.set(id);
	}

	public String getNumber() {
		return number.get();
	}

	public void setNumber(String number) {
		this.number.set(number);
	}

//	public String getValue() {
//		return value.get();
//	}
//
//	public void setValue(String value) {
//		this.value.set(value);
//	}
//	@Override
//	public String toString() {
//		return "Contact{" + "name=" + name + ", id=" + id + ", number=" + number + ", value=" + value + '}';
//	}
	@Override
	public String toString() {
		return "Contact{" + "name=" + name + ", id=" + id + ", number=" + number + '}';
	}
}
