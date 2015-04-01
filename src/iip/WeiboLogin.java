package iip;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WeiboLogin {
	
private CloseableHttpClient httpclient = HttpClients.createDefault();
	
	private String username = "";//微博用户名
	
	private String password = "";//密码
	
	private String vk = "";//需要post的参数之一
	
	private String backURL = "";//需要post的参数之一
	
	private String postUrl = "";//post的url
	
	private String passwordName = "";//需要post的参数之一
	
	
	public WeiboLogin(String username,String password) {
		this.username = username;
		this.password = password;
	}
	
	/**
	 * 登录前的准备工作（获取一些必要的参数）
	 */
	public void getPostValue() {
		try{
		String loginUrl = "http://login.weibo.cn/login/?backURL=&backTitle=&vt=4&revalid=2&ns=1";
		HttpGet httpget = new HttpGet(loginUrl);
		CloseableHttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		String html = EntityUtils.toString(entity);
		Document doc = Jsoup.parse(html);
		Element e = doc.select("form").first();
		postUrl = "http://login.weibo.cn/login/"+e.attr("action");
		Elements elements = e.select("input");
		vk = elements.get(6).attr("value");
		backURL = elements.get(3).attr("value");
		passwordName = elements.get(1).attr("name");
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 登录，向服务器post一些登录params
	 * @param localContext
	 * @return
	 */
	public boolean login(HttpContext localContext) {
		boolean flag = false;
		try{
			HttpPost httppost = new HttpPost(postUrl);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("backTitle", "微博"));
			params.add(new BasicNameValuePair("backURL", backURL));
			params.add(new BasicNameValuePair(passwordName, password));
			params.add(new BasicNameValuePair("mobile", username));
			params.add(new BasicNameValuePair("vk", vk));
			params.add(new BasicNameValuePair("submit", "登录"));
			params.add(new BasicNameValuePair("remember", "on"));
			httppost.setEntity(new UrlEncodedFormEntity(params));
			CloseableHttpResponse response = httpclient.execute(httppost,localContext);
			int statuts_code=response.getStatusLine().getStatusCode();
			System.out.println(statuts_code);
			if(statuts_code==302) flag = true;
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return flag;
	}
	
	/**
	 * 登录成功后获取cookies
	 * @return cookies
	 */
	public String getCookies() {
		StringBuffer cookies = new StringBuffer();
		getPostValue();
		CookieStore cookieStore = new BasicCookieStore();  
		 //创建一个本地上下文信息  
        HttpContext localContext = new BasicHttpContext();  
        //在本地上下问中绑定一个本地存储  
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);  
		if(login(localContext)){
			List<Cookie> list = cookieStore.getCookies();   
			for(Cookie cookie: list) {
				cookies.append(cookie.getName() + "=" + cookie.getValue() + ";");
			}
		}
		else {
			System.out.println("登录失败！");
		}
		
		return cookies.toString();
	}
	
	
	
	
	public static void main(String[] args) {
		WeiboLogin wb = new WeiboLogin("1445715631@qq.com","19921008ay");
		String cookie = wb.getCookies();
		System.out.println(cookie);
	}

}
