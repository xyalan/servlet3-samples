package com.hialan.listener;

import com.hialan.enumes.ConsumerEventType;
import com.hialan.event.ConsumerEvent;
import com.hialan.thread.CCMSThreadFactory;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimpleMessageListener implements MessageListener ,InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(SimpleMessageListener.class);
    private static final String HEADERS_TYPE_KEY = "type";

    @Autowired(required = false)
    private List<ConsumerEventListener> registedListener;

    private List<ThreadPoolExecutor> executorServicesList;

    @Override
    public void afterPropertiesSet() throws Exception {
        String tenantId = "rabbit-mq";
        Assert.notNull(tenantId, "tenantId cannot be null ,check table tb_app_properties  ");
        if(!CollectionUtils.isEmpty(registedListener)){
            executorServicesList = new ArrayList<>();
            for(ConsumerEventListener listener:registedListener){
                ThreadFactory threadFactory =  new CCMSThreadFactory(tenantId,"eventbus",listener.getClass().getSimpleName(),Thread.NORM_PRIORITY+2);
                ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),threadFactory);
                executorServicesList.add(executor);
            }
        }
    }

    @Override
    public void onMessage(Message message) {
        MessageProperties properties = message.getMessageProperties();
        Map<String, Object> map = properties.getHeaders();
        String messageBody = new String(message.getBody());
        logger.info("Received data : {}", messageBody);
        System.out.println("Received data : " + messageBody);

        final ConsumerEventType type = ConsumerEventType.valueOfIgnoreCase((String) map.get(HEADERS_TYPE_KEY));
        logger.info("consumer event type : {}", type.getValue());
        System.out.println("consumer event type : " + type.getValue());

        // write file as log
        writeFileAsLogger(type, messageBody);

        // selection current user's data
        //TODO add by quxiaojing  need to take shutdown into consider,maybe should apply eventsource
        try {
            dispatchExecutor(type, messageBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFileAsLogger(final ConsumerEventType type, final String message) {
        final String fileFullPath = "/Users/Alan/mq.txt";
        try {
            FileUtils.write(new File(fileFullPath), message, false);
        } catch (IOException e) {
            logger.info("write file as logger happend exception.");
        }
    }

    private void dispatchExecutor(final ConsumerEventType type, final String message) throws IOException {
        List<String> validOrders = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(message);
        String[] arr = objectMapper.readValue(jsonNode, String[].class);
        for (String tid : arr) {
            validOrders.add(tid);
        }

        if (!CollectionUtils.isEmpty(validOrders)) {
            notifyConsumerEvent(type, validOrders);
        } else {
            logger.info("this consumer find order list is empty, not to notify all of event " +
                    "listeners");
        }

    }

    private void notifyConsumerEvent(final ConsumerEventType type, final List<String> message) {
        if (!CollectionUtils.isEmpty(registedListener)) {
            for(int i=0;i<registedListener.size();i++){
                final ConsumerEventListener listener = registedListener.get(i);
                final ThreadPoolExecutor executor = executorServicesList.get(i);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            listener.handleEvent(new ConsumerEvent(type, message));
                        } catch (Exception e) {
                            logger.error("handle event happend exception, listener = {};" +
                                    "error:{}" ,listener.getClass(),e);
                            e.printStackTrace();
                        }
                    }
                });
            }
        }else{
            logger.error("got event but no listener regitered :{}",message);
        }
    }

}