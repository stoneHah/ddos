package com.zq.learn.ddos.act;

import com.alibaba.fastjson.JSON;
import com.zq.learn.ddos.greenpower.GreenPowerResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ${DESCRIPTION}
 *
 * @author qun.zheng
 * @create 2018/1/17
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class VoteTest {

    @Autowired
    private RestTemplate restTemplate;

    private ThreadPoolExecutor threadPool;

    @Before
    public void setup() {
        threadPool = new ThreadPoolExecutor(
                1, 16, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1000));
    }

    @Test
    public void testSendValidateCode() throws InterruptedException {
        CountDownLatch doneSignal = new CountDownLatch(1);
        AtomicLong count = new AtomicLong(0);

        threadPool.execute(new VoteTask(doneSignal,count));
        /*phoneNums.forEach(p -> {
            threadPool.execute(new SendValidateTask(p,doneSignal));
        });*/

        doneSignal.await();
    }

    private class VoteTask implements Runnable {

        private CountDownLatch doneSignal;
        private AtomicLong count;

        public VoteTask(CountDownLatch doneSignal, AtomicLong count) {
            this.doneSignal = doneSignal;
            this.count = count;
        }

        @Override
        public void run() {
            while (count.get() < 1000){
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("d", "{\"cvs\":{\"i\":200230833,\"t\":\"gWlyJwa\",\"s\":200677754,\"acc\":\"2zIgPg2K10JttEEWgpVW3jrvoOH05So8\",\"r\":\"\",\"c\":{\"cp\":{\"202348450\":202037122}}}}");

                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

                try {
                    String res = restTemplate.postForObject("http://p4rdli3kqakqx8sy.mikecrm.com/handler/web/form_runtime/handleSubmit.php", request, String.class);
                    count.incrementAndGet();
                    System.out.println("请求成功," + count.get() + ":" +res);
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("发送失败...");
                }

                try {
                    TimeUnit.SECONDS.sleep((long) (5 + Math.random() * 10));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            doneSignal.countDown();

        }
    }


    @After
    public void shutdown() {
        threadPool.shutdown();
    }

}
