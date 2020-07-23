package rpc;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import db.MySQLConnection;
import entity.Item;

/**
 * Servlet implementation class ItemHistory2
 */
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if(session == null) {
			response.setStatus(403);
			return;
		}
		
		String userId = request.getParameter("user_id");
		MySQLConnection conn = new MySQLConnection();
		Set<Item> favoriteItems = conn.getFavoriteItems(userId);
		
		JSONArray array = new JSONArray();
		for (Item item : favoriteItems) {
			JSONObject object = item.toJSONObject();
			// 这个favorite 是给 前端用来更改图标的
			object.put("favorite", true);
			array.put(object);
		}
		RpcHelper.writeJsonArray(response, array);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if(session == null) {
			response.setStatus(403);
			return;
		}
		
		JSONObject input = new JSONObject(IOUtils.toString(request.getReader()));
		String userId = input.getString("user_id");
		Item item = RpcHelper.parseFavoriteItem(input.getJSONObject("favorite"));
		
		
		MySQLConnection connection = new MySQLConnection();
		connection.setFavoriteItems(userId, item);
		connection.close();
		
		RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if(session == null) {
			response.setStatus(403);
			return;
		}
		
		
		JSONObject input = new JSONObject(IOUtils.toString(request.getReader()));
		String userId = input.getString("user_id");
		Item item = RpcHelper.parseFavoriteItem(input.getJSONObject("favorite"));
		
		
		MySQLConnection connection = new MySQLConnection();
		connection.unsetFavoriteItems(userId, item.getItemId());
		connection.close();
		
		RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
	}

}
