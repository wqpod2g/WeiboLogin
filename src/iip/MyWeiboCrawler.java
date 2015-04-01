package iip;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class MyWeiboCrawler {
	
	public int count = 0;
	
	/**
	 * 从WeiboURL文件中读取待爬取微博用户URL
	 * @return
	 */
	public List<String> getUserURLs() {
		List<String> URLs = new ArrayList<String>();
		try {
			FileInputStream fs = new FileInputStream("UserURL.txt");
			InputStreamReader is=new InputStreamReader(fs,"utf-8");
			BufferedReader br=new BufferedReader(is);
			String line=br.readLine();
			while(line!=null){
				URLs.add(line);
				line=br.readLine();
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return URLs;
	}
	
	/**
	 * 从WeiboID.txt中获取一个微博帐号用于登录
	 * @return
	 */
	public List<String> getWeiboID() {
		List<String> list = new ArrayList<String>();
		List<String> allID = new ArrayList<String>();
		try {
			FileInputStream fs = new FileInputStream("WeiboID.txt");
			InputStreamReader is=new InputStreamReader(fs,"utf-8");
			BufferedReader br=new BufferedReader(is);
			String line=br.readLine();
			while(line!=null){
				allID.add(line);
				line=br.readLine();
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		int random = new Random().nextInt(allID.size());
		String line = allID.get(random);
		String[] array = line.split(",");
		list.add(array[0]);
		list.add(array[1]);
		return list;
	}
	
	
	/**
	 * 获取某个用户微博总页数
	 * @param URL
	 * @param cookie
	 * @return
	 */
	public int getSumPages(String URL,String cookie) {
		int sumPages = 0;
		String html = "";
		html = getHTML(URL,cookie);
		Document doc = Jsoup.parse(html);
		Element e = doc.select("input[name=mp]").first();
		if(e!=null) {
			String value = e.attr("value");
			sumPages = Integer.valueOf(value);
		}
		else {
			System.out.println("获取总页数失败！");
		}
		return sumPages;
	}
	
	/**
	 * 获取所有用户URL并依次爬取
	 * @param cookie
	 */
	public void getWeibo() {
		List<String> userURLs = new ArrayList<String>();
		userURLs = getUserURLs();
		for(String userURL:userURLs) {
			getUserWeibo(userURL);
		}
	}
	
	/**
	 * 爬取某个用户weibo
	 * @param userURL
	 * @param cookie
	 * @throws Exception 
	 */
	public void getUserWeibo(String userURL) {
		String cookie = "";
		int sumPages = 0;
		try{
//			while(true) {
//				List<String> ID = getWeiboID();
//			    cookie = WeiboCN.getSinaCookie(ID.get(0), ID.get(1));
//		        sumPages = getSumPages(userURL,cookie);
//		        System.out.println("sumPages="+sumPages);
//		        if(sumPages!=0) break;
//		        }
			 WeiboLogin wb = new WeiboLogin("wqchina007@126.com", "7027887");
			 cookie = wb.getCookies();
		     sumPages = getSumPages(userURL,cookie);
		     System.out.println("sumPages="+sumPages);
			 for(int i=1;i<=sumPages;i++) {
		    	String url = userURL+"?page="+i;
		    	String html = getHTML(url,cookie);
			    parse(html);
			    count++;
			    System.out.println("*************"+userURL+"的第"+i+"条微博抓取完毕************"+count);
		    }
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取html
	 * @param cookie
	 * @param url
	 * @return
	 */
	public String getHTML(String url,String cookie)
	{
		String html = "";
		HttpClient httpclient = new DefaultHttpClient();  
		HttpPost get = new HttpPost(url);  
        get.setHeader("Cookie", cookie);  
        try {
        	HttpResponse response = httpclient.execute(get);  
//        	Header[] heads = response.getAllHeaders();
//        	for(Header h:heads){  
//                System.out.println(h.getName()+":"+h.getValue());  
//            }  
            HttpEntity entity = response.getEntity();  
			html = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            } catch (Exception e) {
            	e.printStackTrace();
            } finally {
            	get.releaseConnection();
            }
        return html;
	}
	
	private void parse(String html){
		try {
			Document doc = Jsoup.parse(html);
			Elements weibos = doc.select("div.c");  
			for(Element weibo:weibos){  
				
				String text="";
				String commentText = "";
		    	Elements divs = weibo.select("div");
		    	for(Element div:divs) {
		    		String str = div.text();
		    		if(str.contains("转发理由")) {
		    			commentText = str.replaceAll("赞\\[\\d+\\].+", "");
		    		}
		    	}
		    	
		    	text=weibo.getElementsByClass("ctt").text();
		    	
		    	String allText = commentText+"////"+text;
		    	//System.out.println("size="+size+"index="+index);
				System.out.println(allText);  
			}  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		MyWeiboCrawler mwc = new MyWeiboCrawler();
		mwc.getWeibo();
	}
}
