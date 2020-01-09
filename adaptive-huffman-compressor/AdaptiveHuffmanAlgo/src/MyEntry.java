import java.util.Map;

final class MyEntry implements Map.Entry<String,Integer> {
	
	private final String key;
	private Integer value;
	
	public MyEntry(String key, Integer value) {
		this.key=key;
		this.value=value;
	}
	@Override
	public String getKey() {
		return key;
	}

	@Override
	public Integer getValue() {
		return value;
	}

	@Override
	public Integer setValue(Integer value) {
		Integer old=this.value;
		this.value=value;
		return old;
	}	

}
