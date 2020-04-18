package com.example.jdk11demo.utils;

import io.etcd.jetcd.*;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName EtcdUtils
 * @Author W.Z
 * @Date 2020/4/18 11:43
 **/
@Slf4j
public class EtcdUtils {

    private static final int maxEvents = Integer.MAX_VALUE;
    //etcl客户端链接
    private static Client client = null;

    /**
     * 根据指定的配置名称获取对应的value
     * @param key 配置项
     * @return
     * @throws Exception
     */
    public static String getEtcdValueByKey(String key) throws Exception {
        List<KeyValue> kvs = EtcdUtils.getEtclClient().getKVClient().get(ByteSequence.from(key,StandardCharsets.UTF_8)).get().getKvs();
        if(kvs.size()>0){
            return kvs.get(0).getValue().toString(StandardCharsets.UTF_8);
        }
        else {
            return null;
        }
    }
    /**
     * 新增或者修改指定的配置
     * @param key
     * @param value
     * @return
     */
    public static void putEtcdValueByKey(String key,String value){
        EtcdUtils.getEtclClient().getKVClient().put(ByteSequence.from(key,StandardCharsets.UTF_8),ByteSequence.from(value.getBytes(StandardCharsets.UTF_8)));
    }
    /**
     * 删除指定的配置
     * @param key key
     * @return
     */
    public static void deleteEtcdValueByKey(String key){
        EtcdUtils.getEtclClient().getKVClient().delete(ByteSequence.from(key,StandardCharsets.UTF_8));
    }

    /**
     * 监听指定key的变化
     * @param keyString key
     */
    public static void watchKey(String keyString){

        CountDownLatch latch = new CountDownLatch(maxEvents);
        ByteSequence key = ByteSequence.from(keyString, StandardCharsets.UTF_8);
        Watch.Listener listener = Watch.listener(response -> {
            log.info("Watching for key={}", key);

            for (WatchEvent event : response.getEvents()) {
                log.info("type={}, key={}, value={}", event.getEventType(),
                        Optional.ofNullable(event.getKeyValue().getKey()).map(bs -> bs.toString(StandardCharsets.UTF_8)).orElse(""),
                        Optional.ofNullable(event.getKeyValue().getValue()).map(bs -> bs.toString(StandardCharsets.UTF_8)).orElse(""));
            }

            latch.countDown();
        });

        try (Watch watch = getEtclClient().getWatchClient();
             Watch.Watcher watcher = watch.watch(key, listener)) {

            latch.await();
        } catch (Exception e) {
            log.error("Watching Error {}", e);
            System.exit(1);
        }
    }


    //链接初始化 单例模式
    private static synchronized Client getEtclClient(){
        if(null == client){
            client = Client.builder().endpoints("http://127.0.0.1:2379").build();
        }
        return client;
    }
    private static String getConfig(List<KeyValue> kvs){
        if(kvs.size()>0){
            String config = kvs.get(0).getKey().toString(StandardCharsets.UTF_8);
            String value = kvs.get(0).getValue().toString(StandardCharsets.UTF_8);
            log.info("etcd 's config 's config key is :{},value is:{}",config,value);
            return value;
        }
        else {
            return null;
        }
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(()->{
            watchKey("test");
        });
        putEtcdValueByKey("test","aaa");
        String test = getEtcdValueByKey("test");
        System.out.println(test);;
    }
}
