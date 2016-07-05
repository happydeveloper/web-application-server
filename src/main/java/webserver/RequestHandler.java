package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequestUtils;
import util.IOUtils;
import db.DataBase;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	
	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
		HttpRequest req = new HttpRequest();
		HttpResponse rsp = new HttpResponse();
		
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String line = br.readLine();
			if (line == null) {
				return;
			}

			log.debug("request line : {}", line);
			String[] tokens = line.split(" ");

			int contentLength = 0;
			boolean logined = false;
			while (!line.equals("")) {
				line = br.readLine();
				log.debug("header : {}", line);
				
				if (line.contains("Content-Length")) {
					contentLength = getContentLength(line);
				}
				
				if (line.contains("Cookie")) {
					logined = isLogin(line);
				}
			}

			String url = getDefaultUrl(tokens);
			
			//req.getRequest(url, br, contentLength, out, rsp);
			
			// BufferedReader br, int contentLength, OutputStream out, HttpResponse res
			
			if ("/user/list".equals(url)) {
				if (!logined) {
					rsp.responseResource(out, "/user/login.html");
					return;
				}

				Collection<User> users = DataBase.findAll();
				StringBuilder sb = new StringBuilder();
				sb.append("<table border='1'>");
				for (User user : users) {
					sb.append("<tr>");
					sb.append("<td>" + user.getUserId() + "</td>");
					sb.append("<td>" + user.getName() + "</td>");
					sb.append("<td>" + user.getEmail() + "</td>");
					sb.append("</tr>");
				}
				sb.append("</table>");
				byte[] body = sb.toString().getBytes();
				DataOutputStream dos = new DataOutputStream(out);
				rsp.response200Header(dos, body.length);
				rsp.responseBody(dos, body);
			} else if (url.endsWith(".css")) {
				rsp.responseCssResource(out, url);
			} else {
				rsp.responseResource(out, url);
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private boolean isLogin(String line) {
		String[] headerTokens = line.split(":");
		Map<String, String> cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
		String value = cookies.get("logined");
		if (value == null) {
			return false;
		}
		return Boolean.parseBoolean(value);
	}

	

	

	private int getContentLength(String line) {
		String[] headerTokens = line.split(":");
		return Integer.parseInt(headerTokens[1].trim());
	}

	private String getDefaultUrl(String[] tokens) {
		String url = tokens[1];
		if (url.equals("/")) {
			url = "/index.html";
		}
		return url;
	}

	

	
}
