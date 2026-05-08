package com.demo.order.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单号生成器
 */
@Component
@Slf4j
public class OrderNoGenerator {

    @Value("${app.order.prefix:ORD-}")
    private String orderPrefix;

    @Value("${app.order.date-format:yyyyMMdd}")
    private String dateFormat;

    @Value("${app.order.sequence-length:4}")
    private int sequenceLength;

    private final AtomicInteger counter = new AtomicInteger(1);
    private String currentDate;
    private final Object lock = new Object();

    /**
     * 生成订单号
     * 格式：ORD-yyyyMMdd-xxxx
     */
    public String generate() {
        synchronized (lock) {
            String today = getToday();
            
            // 如果日期变化，重置计数器
            if (!today.equals(currentDate)) {
                currentDate = today;
                counter.set(1);
                log.info("日期变更，重置订单号计数器，当前日期：{}", today);
            }
            
            // 生成序列号
            int seq = counter.getAndIncrement();
            if (seq > getMaxSequence()) {
                counter.set(1);
                seq = 1;
                log.warn("序列号超过最大值，重置为1");
            }
            
            String orderNo = formatOrderNo(today, seq);
            log.debug("生成订单号：{}", orderNo);
            return orderNo;
        }
    }

    /**
     * 获取当前日期字符串
     */
    private String getToday() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(dateFormat));
    }

    /**
     * 格式化订单号
     */
    private String formatOrderNo(String date, int sequence) {
        String seqStr = String.format("%0" + sequenceLength + "d", sequence);
        return orderPrefix + date + "-" + seqStr;
    }

    /**
     * 获取最大序列号
     */
    private int getMaxSequence() {
        return (int) Math.pow(10, sequenceLength) - 1;
    }

    /**
     * 批量生成订单号
     */
    public String[] batchGenerate(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("生成数量必须大于0");
        }
        if (count > 1000) {
            throw new IllegalArgumentException("单次生成数量不能超过1000");
        }
        
        String[] orderNos = new String[count];
        for (int i = 0; i < count; i++) {
            orderNos[i] = generate();
        }
        return orderNos;
    }

    /**
     * 验证订单号格式
     */
    public boolean isValidOrderNo(String orderNo) {
        if (orderNo == null || orderNo.length() != orderPrefix.length() + dateFormat.length() + 1 + sequenceLength) {
            return false;
        }
        
        if (!orderNo.startsWith(orderPrefix)) {
            return false;
        }
        
        try {
            String datePart = orderNo.substring(orderPrefix.length(), orderPrefix.length() + dateFormat.length());
            String seqPart = orderNo.substring(orderPrefix.length() + dateFormat.length() + 1);
            
            // 验证日期部分
            LocalDate.parse(datePart, DateTimeFormatter.ofPattern(dateFormat));
            
            // 验证序列号部分
            int seq = Integer.parseInt(seqPart);
            return seq >= 1 && seq <= getMaxSequence();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从订单号中提取日期
     */
    public LocalDate extractDateFromOrderNo(String orderNo) {
        if (!isValidOrderNo(orderNo)) {
            throw new IllegalArgumentException("无效的订单号格式：" + orderNo);
        }
        
        String datePart = orderNo.substring(orderPrefix.length(), orderPrefix.length() + dateFormat.length());
        return LocalDate.parse(datePart, DateTimeFormatter.ofPattern(dateFormat));
    }

    /**
     * 从订单号中提取序列号
     */
    public int extractSequenceFromOrderNo(String orderNo) {
        if (!isValidOrderNo(orderNo)) {
            throw new IllegalArgumentException("无效的订单号格式：" + orderNo);
        }
        
        String seqPart = orderNo.substring(orderPrefix.length() + dateFormat.length() + 1);
        return Integer.parseInt(seqPart);
    }

    /**
     * 获取今天的订单号数量
     */
    public int getTodayOrderCount() {
        synchronized (lock) {
            String today = getToday();
            if (!today.equals(currentDate)) {
                return 0;
            }
            return counter.get() - 1; // 因为getAndIncrement会先获取再增加
        }
    }

    /**
     * 重置计数器（主要用于测试）
     */
    public void resetCounter() {
        synchronized (lock) {
            counter.set(1);
            currentDate = null;
            log.info("订单号计数器已重置");
        }
    }

    /**
     * 获取下一个订单号（不增加计数器）
     */
    public String peekNextOrderNo() {
        synchronized (lock) {
            String today = getToday();
            int currentSeq = counter.get();
            return formatOrderNo(today, currentSeq);
        }
    }
}