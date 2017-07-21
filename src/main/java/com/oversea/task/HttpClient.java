package com.oversea.task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HttpClient {

    private static final ExecutorService service = Executors.newCachedThreadPool();

    public static byte[] httpGet(final String urlStr) {
        try {
            URL url = new URL(urlStr);
            final HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            Future<byte[]> future = service.submit(new Callable<byte[]>() {
                public byte[] call() throws Exception {
                    try {
                        httpConn.setDoInput(true);
                        httpConn.setDoOutput(true);
                        httpConn.setUseCaches(false);
                        httpConn.setConnectTimeout(30000);
                        httpConn.setReadTimeout(30000);
                        httpConn.setAllowUserInteraction(false);
                        httpConn.connect();
                        byte[] data = readResponseData(httpConn);
                        return data;
                    } catch (Throwable e) {
                        System.out.println(e.getMessage());
                        return null;
                    }
                }
            });
            //最多执行2分钟必须要结束
            try {
                return future.get(300, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                httpConn.disconnect();
                System.out.println(e.getMessage());
            }
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        byte[] aa = httpGet("http://122.225.114.20:3401/upgrade_jar");
        System.out.println(new String(aa));
    }

    private static byte[] readResponseData(HttpURLConnection httpConn) throws IOException {
        ByteArrayOutputStream baos = null;
        InputStream in = null;
        byte[] buffer = new byte[1024];
        long total = httpConn.getContentLength();
        in = httpConn.getInputStream();
        baos = new ByteArrayOutputStream();
        int len = 0;
        long read = 0;
        while ((len = in.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
            read += len;
            showProgress(total, read);
        }

        byte[] data = null;

        data = baos.toByteArray();

        if (null != in) in.close();
        if (null != baos) baos.close();
        httpConn.disconnect();
        return data;
    }
    
    static String[] perArr = { "0.10", "0.20", "0.30", "0.40", "0.50", "0.60", "0.70", "0.80", "0.90", "1.00" };
    
    static Set<String> set = new LinkedHashSet<String>();
    static int  setlen = 0;
	
	private static void showProgress(long total, long read)
	{
		float p = read * 1.0F / total;
		String str = String.format("%.2f", p);
		for (String x : perArr)
		{
			if (str.equals(x))
			{
				str = String.format("%.2f", Float.valueOf(str) * 100) + "%";
				str = String.format("%10s", str);
				set.add(str);
				if(set.size()!=setlen){
					Object[] m = set.toArray();
					String msg = "now :"+m[m.length-1]+" ";
					for(int i=0;i<set.size();i++){
						msg+= ".";
					}
					System.out.println(msg);
				}
				setlen = set.size();
			}
		}
	}
}
