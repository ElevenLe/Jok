package db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import entity.Item;
import entity.Item.ItemBuilder;

public class MySQLConnection {
	private Connection conn;

	public MySQLConnection() {
		try {
			// 第一句话是sql 自带的问题，必须这么声明才能继续使用sql java
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 储存喜爱关系到数据库
	public void setFavoriteItems(String userID, Item item) {
		if (conn == null) {
			return;
		}
		
		// 不需要save user，因为一定login 了之后才会call 这个function，那么
		saveItem(item); // 根据foreign key 原则，item 必须存在在item table 里面，所有要先save一下到item table里
		// Maybe Insert item to items table
		String sql = "INSERT INTO history (user_id, item_id) VALUES (?, ?)";
		
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userID);
			statement.setString(2, item.getItemId());
			statement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 删除收藏数据库关系
	// 如果只有一个人喜欢一个job，这个job被取消收藏，最好还是删掉，因为会浪费存储空间
	// 小技巧，删除程序不需要写这儿，可以写在一个route program，也就是一个星期一检查删除的程序，来提高返回速度
	public void unsetFavoriteItems(String userID, String itemId) {
		if (conn == null) {
			return;
		}
		String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ？";
		PreparedStatement statement;
		try {
			statement = conn.prepareStatement(sql);
			statement.setString(1, userID);
			statement.setString(2, itemId);
			statement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveItem(Item item) {
		if (conn == null) {
			return;
		}
		// 如果很多人同时喜欢一个item，这个item 都要save。 现在虽然不会重复覆盖，但是sql 会报错
		// 使用 ignore 就会省略这个问题
		String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?)";
		// 这里业务逻辑是如果id 一样，那么内容变了也当成同一个job 和内容
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setString(3, item.getAddress());
			statement.setString(4, item.getImageUrl());
			statement.setString(5, item.getUrl());
			statement.executeUpdate();
			
			// keywords 是一个单独table，需要重新储存
			sql = "INSERT IGNORE INTO keywords VALUES (?, ?)";
            statement = conn.prepareStatement(sql);
            statement.setString(1, item.getItemId());
            for (String keyword : item.getKeyword()) {
            	statement.setString(2, keyword);
            	statement.executeUpdate();
            }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Set<String> getFavoriteItemIds(String userId){
		if (conn == null) {
			return new HashSet<>();
		}
		Set<String> favoriteItemIds = new HashSet<>();
		String sql = "SELECT item_id FROM history WHERE user_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs =  statement.executeQuery();
			while(rs.next()) {
				String itemId = rs.getString("item_id");
				favoriteItemIds.add(itemId);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return favoriteItemIds;
	}
	
	public Set<Item> getFavoriteItems(String userId){
		if (conn == null) {
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();

		// Get favorite item ids
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);
		// Get Item based item id
		for (String itemId : favoriteItemIds) {
			String sql = "SELECT *  FROM items WHERE item_id = ?";
			try {
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, itemId);
				ResultSet rs =  statement.executeQuery();
				// 这里 while 和 if 没有区别，因为rs 只有一个item
				// 但是未来有可能改变业务，也就是item 可能重复
				while(rs.next()) {
					// get keywords based on the item id
					ItemBuilder builder = new ItemBuilder();
					builder.setItemId(rs.getString("item_id"));
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setKeyword(getKeywords(itemId));
					favoriteItems.add(builder.build());
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Get keywords based on item id
		
		return favoriteItems;
	}
	
	public Set<String> getKeywords(String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return null;
		}
		Set<String> keywords = new HashSet<>();
		String sql = "SELECT keyword from keywords WHERE item_id = ? ";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String keyword = rs.getString("keyword");
				keywords.add(keyword);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return keywords;
	}
	
	public String getFullname(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return "";
		}
		String name = "";
		String sql = "SELECT first_name, last_name FROM users WHERE user_id = ? "; // 容易出错，最好copy
		
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId); // 1 这里是第一个问好
			ResultSet rs = statement.executeQuery();
			// .next() 读出来有就拿出，但是如果没有就直接跳过了。if 因为因为只有一个user 读出来了
			if(rs.next()) {
				name = rs.getString("first_name") + " " + rs.getString("last_name");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return name;
	}
	
	// verify 要分类讨论，因为hacker 会测试用户存不存在
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		
		String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
		
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			return rs.next();
//			if(rs.next()) {
//				return true;
//			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}
	
	public boolean addUser(String userId, String password, String firstName, String lastName) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		
		// 注意这里使用了 ignore 
		String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			statement.setString(3, firstName);
			statement.setString(4, lastName);

			return statement.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;				
	}
}
