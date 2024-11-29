package org.example;

import javax.swing.*;
import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

// 按两次 Shift 打开“随处搜索”对话框并输入 `show whitespaces`，
// 然后按 Enter 键。现在，您可以在代码中看到空格字符。
public class Main {
    public static void main(String[] args) {
        // 定义要增加的秒数时间戳
        long secondsToAdd = 1729484761L;

        // 获取Unix纪元的时间点（1970年1月1日）
        Instant unixEpoch = Instant.EPOCH;

        // 使用Duration类来增加秒数
        Instant newTime = unixEpoch.plus(Duration.ofSeconds(secondsToAdd));

        // 将Instant转换为LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(newTime, ZoneId.systemDefault());

        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 打印格式化后的日期时间
        System.out.println("加上秒数后的时间: " + formatter.format(dateTime));
    }
}