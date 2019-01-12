package name.wexler;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        queue.add(url);

        Callable<String> callableTask = () -> {
            URLConnection connection = url.openConnection();

            connection.setRequestProperty("Cookie",
                    StringUtils.join(cookieManager.getCookieStore().getCookies(), ';'));

            InputStream is = connection.getInputStream();
            BufferedInputStream reader = new BufferedInputStream(is);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
            String contents = buffer.lines().collect(Collectors.joining("\n"));
            processResponse(url, contents, connection.getHeaderFields());
            Map<String, List<String>> headerFields = connection.getHeaderFields();

            cookieManager.put(url.toURI(), headerFields);
            return "get" + url;
        };
        executorService.submit(callableTask);
    }

    public URL getNextURL() {
        return queue.remove();
    }
}
