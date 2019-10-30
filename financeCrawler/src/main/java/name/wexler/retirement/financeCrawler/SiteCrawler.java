package name.wexler.retirement.financeCrawler;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

abstract public class SiteCrawler {
    private static final String SET_COOKIE_HEADER = "set-cookie";
    private final Queue<URL> queue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService;
    private final CookieManager cookieManager;


    public SiteCrawler() {
        CookieStore cookieStore = new NonexpiringCookieStore();
        cookieManager = new java.net.CookieManager(cookieStore, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        executorService = getExecutorService();
    }

    abstract public ExecutorService getExecutorService();
    abstract public void processResponse(URL url, String contents, Map<String, List<String>> headers);

    public void crawlURL(URL url) {
        invokeURL(url, "GET", "");
    }

    public void postURL(URL url, String content) {
        invokeURL(url, "POST", content);
    }

    private void invokeURL(URL url, String method, String content) {
        Callable<String> callableTask = () -> {
            System.out.println("Sending request to " + url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);

            conn.setRequestProperty("Cookie",
                    StringUtils.join(cookieManager.getCookieStore().getCookies(), ';'));
            conn.setRequestProperty("Content-Type", "application/json");

            if (content.length() > 0) {
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(content);
                writer.flush();
                writer.close();
            }

            try {
                InputStream is = conn.getInputStream();
                BufferedInputStream reader = new BufferedInputStream(is);
                BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
                String contents = buffer.lines().collect(Collectors.joining("\n"));
                processResponse(url, contents, conn.getHeaderFields());
                Map<String, List<String>> headerFields = conn.getHeaderFields();

                cookieManager.put(url.toURI(), headerFields);
                reader.close();
            } catch (FileNotFoundException fnfe) {
                System.out.println("Caught exception: " + fnfe);
            }

            return method + url;
        };
        executorService.submit(callableTask);
    }
}
