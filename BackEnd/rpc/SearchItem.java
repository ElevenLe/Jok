package rpc;
import external.GithubClient;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import db.MySQLConnection;
import entity.Item;

/**
 * Servlet implementation class SearchItem
 */
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// what is contain type
//		response.setContentType("application/json");

//		PrintWriter writer = response.getWriter();
//		if (request.getParameter("username") != null) {
//			JSONObject obj = new JSONObject();
//			String username = request.getParameter("username");
//			obj.put("username", username);
//			writer.print(obj);
//		}

//		JSONArray array = new JSONArray();
//		array.put(new JSONObject().put("username", "abcd"));
//		array.put(new JSONObject().put("username", "1234"));
////		writer.print(array);
//		RpcHelper.writeJsonArray(response, array);
		
		
		HttpSession session = request.getSession(false);
		if(session == null) {
			response.setStatus(403);
			return;
		}
		
		String userId = request.getParameter("user_id");
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		
		MySQLConnection connection = new MySQLConnection();
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
		connection.close();
		
		GithubClient client = new GithubClient();
		List<Item> itemList = client.search(lat, lon, null);
		JSONArray array = new JSONArray();
		
		for(Item item: itemList) {
			JSONObject obj = item.toJSONObject();
			obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
			array.put(obj);
		}
		RpcHelper.writeJsonArray(response, array);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		if(session == null) {
			response.setStatus(403);
			return;
		}
		doGet(request, response);
	}

}
