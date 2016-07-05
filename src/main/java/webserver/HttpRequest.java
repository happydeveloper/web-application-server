package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {

	public void getRequest(String req){
		
	}
	
	public void requestCreate(BufferedReader br, int contentLength, OutputStream out,  HttpResponse res  ) throws IOException
	{	
		String body = IOUtils.readData(br, contentLength);
		Map<String, String> params = HttpRequestUtils.parseQueryString(body);
		User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
		//log.debug("user : {}", user);
		DataBase.addUser(user);
		DataOutputStream dos = new DataOutputStream(out);
		res.response302Header(dos);
	}
	
	public void requestUser(BufferedReader br, int contentLength, OutputStream out,  HttpResponse res) throws IOException
	{
		String body = IOUtils.readData(br, contentLength);
		Map<String, String> params = HttpRequestUtils.parseQueryString(body);
		User user = DataBase.findUserById(params.get("userId"));
		if (user != null) {
			if (user.login(params.get("password"))) {
				DataOutputStream dos = new DataOutputStream(out);
				res.response302LoginSuccessHeader(dos);
			} else {
				res.responseResource(out, "/user/login_failed.html");
			}
		} else {
			res.responseResource(out, "/user/login_failed.html");
		}
	}

	public void request(String url, BufferedReader br, int contentLength, OutputStream out, HttpResponse res) {
		// TODO Auto-generated method stub
		if ("/user/create".equals(url)) {
			//this.requestCreate(br, contentLength, out, res);requestCreate(br, contentLength, out, res);
		} else if ("/user/login".equals(url)) {
			
		}
	}
}
