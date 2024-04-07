public enum TYPE {
	INT("i32"),
	LONG("i64"),
	FLOAT32("float"),
	FLOAT64("double");

	public String type;

	TYPE(String type) {
		this.type = type;
	}
}
