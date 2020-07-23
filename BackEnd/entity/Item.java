package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class Item {
	private String itemId;
	private String name;
	private String address;
	private Set<String> keywords;
	private String imageUrl;
	private String url;
	
	// 不能public， 如果public 就会导致重复的问题出现
	private Item(ItemBuilder builder) {
		this.itemId = builder.itemId;
		this.name = builder.name;
		this.address = builder.address;
		this.keywords = builder.keywords;
		this.imageUrl = builder.imageUrl;
		this.url = builder.url;
	}
	
	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		obj.put("item_id", itemId);
		obj.put("name", name);
		obj.put("address", address);
		obj.put("keywords", new JSONArray(keywords));
		obj.put("image_url", imageUrl);
		obj.put("url", url);
		return obj;
	}

	
	public String getItemId() {
		return itemId;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAddress() {
		return address;
	}

	public Set<String> getKeyword() {
		return keywords;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getUrl() {
		return url;
	}

	// builder pattern 可以让constructor parameter 更灵活
	// design pattern

	// static 是严格规定。内部类
	public static class ItemBuilder {
		private String itemId;
		private String name;
		private String address;
		private Set<String> keywords;
		private String imageUrl;
		private String url;

		public void setItemId(String itemId) {
			this.itemId = itemId;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public void setKeyword(Set<String> keyword) {
			this.keywords = keyword;
		}

		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Item build() {
			return new Item(this);
		}

	}
	
	
	// Item item = new Item(id, name, null, null, null, null)
	// Item item = new Item(id, name)
	// 这样构造的问题在于需要创建很多很多个constructor 来实现多种要求。 现在直接使用builder 就可以解决
	
	// ItemBuilder builder = new ItemBuilder() 
	// 如果没有static 这步会有错 // 错误在如果没有static， 那么需要先建立Item 再建立Itembuilder
	// 如果在class 外面建立builder class，那么会不能调用item 的constructor了
	// builder.setItemId("abcd")
	// builder.setName('vincent')
	// Item item = bulider.bulid()
}
