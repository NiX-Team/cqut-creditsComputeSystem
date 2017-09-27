import com.nix.util.HttpsClient;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by 11723 on 2017/9/21.
 */
public class ConcurrentTest {

    @Test
    public void concurrentTest() {
        final AtomicLong atomicLong = new AtomicLong(0);
        final AtomicLong atomicLong1 = new AtomicLong(0);


        int n = 50,m = 100;


        long now = System.currentTimeMillis();
        for (int i = 0;i < n;i ++ ) {
            new Thread(() -> {
                for (int j = 0;j < m;j ++) {
                    try {
                        Map map = new HashMap();
                        map.put("key","吴林勇");
                        HttpsClient.doGet("http://59.110.234.213/vote/system/vote.do?key=",map);
//                        HttpsClient.doGet("http://59.110.234.213/vote/system/vote.do?",map);
//                        HttpResponse response = HTTP_CLIENT.execute(get);
                        atomicLong.getAndAdd(1);
//                        Thread.sleep(200);
//                        System.out.println(Thread.currentThread().getId() + " : " + response.getStatusLine().getStatusCode());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }

//        for (int j = 0;j < 10000;j ++) {
//            HttpGet get = new HttpGet("http://59.110.234.213/vote/system/vote.do?key=吴林勇");
//            try {
//                HttpResponse response = HTTP_CLIENT.execute(get);
////                        System.out.println(Thread.currentThread().getId() + " : " + response.getStatusLine().getStatusCode());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            atomicLong.getAndIncrement();
//            System.out.println("count = " + atomicLong.longValue() );
//        }

        while (atomicLong.intValue() < n*m) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("count = " + atomicLong.longValue() );
            System.out.println("thread = " + Thread.activeCount() );
        }
        System.out.println("耗时：" + (System.currentTimeMillis() - now));
    }
}
