package com.example.httpdownloadserver.service.impl;

public class DownloadTask implements Runnable {
    private String fileUrl;
    private String destination;

    public DownloadTask(String fileUrl, String destination) {
        this.fileUrl = fileUrl;
        this.destination = destination;
    }

    @Override
    public void run() {
        //任务逻辑：下载任务
    }
}
//线程逻辑：在主线程或线程池中执行任务逻辑
//    DownloadTask task = new DownloadTask("","");
//    Thread thread = new Thread(task);
//    thread.start();
//}
