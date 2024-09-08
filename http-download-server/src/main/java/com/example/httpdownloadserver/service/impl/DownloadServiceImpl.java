package com.example.httpdownloadserver.service.impl;
import com.example.httpdownloadserver.dao.SettingsDAO;
import com.example.httpdownloadserver.dao.TaskDAO;
import com.example.httpdownloadserver.model.Task;
import com.example.httpdownloadserver.service.DownloadService;
import com.google.common.util.concurrent.RateLimiter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DownloadServiceImpl implements DownloadService {
    @Autowired
    private SettingsDAO settingsDAO;
    @Autowired
    private TaskDAO taskDAO;
    private static final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    //创建okhttp实例
    private static final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).build();
    @Override
    public void download(Task task, int threadNum) throws IOException {
        SseEmitter emitter = new SseEmitter();
        emitters.put(task.getId().toString(), emitter);
        //创建一个Request对象
        Request request = new Request.Builder().url(task.getDownloadLink()).build();
        //使用okhttpClient发送请求
        Response response = client.newCall(request).execute();
        try (response) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file:" + response);
            }
            String directoryPath = settingsDAO.selectByName("downloadPath").getSettingValue();
            String fullPath = getString(task, directoryPath);
            //获得正确的路径
            task.setDownloadPath(saveFilePath(directoryPath, fullPath));
        }
        //获取文件大小 单位字节
        String contentLen = response.header("Content-Length");
        if (contentLen == null) {
            throw new IOException("Content len is null");
        }
        Long fileSize = Long.parseLong(contentLen);
        //获得切片大小
        int sliceSize = sliceSize(fileSize);
        //切片个数
        int sliceNum = (int) Math.ceil((double) fileSize / sliceSize);
        //rateLimiter实现限速
        String speed = settingsDAO.selectByName("downloadSpeed").getSettingValue();//MB/s
        RateLimiter rateLimiter = RateLimiter.create(Double.parseDouble(speed) * 1024 * 1024);//每秒不超过指定的下载速度对应的字节数
        AtomicLong downloaded = new AtomicLong(0);
        AtomicInteger currentSlice = new AtomicInteger(0);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadNum);
        int sliceIndex = task.getCurrentSlice();//断点续传
        for (int i = sliceIndex; i < sliceNum; i++) {
            executor.setCorePoolSize(taskDAO.getThreadById(task.getId()));
            long startIndex = (long) i * sliceSize;
            long endIndex = (i == sliceNum - 1) ? fileSize - 1 : startIndex + sliceSize - 1;
            executor.execute(new DownloadTask(task, task.getDownloadPath(), startIndex, endIndex, downloaded, fileSize, currentSlice, taskDAO, rateLimiter, emitter, sliceNum));//线程逻辑：负责任务调度
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                executor.shutdown();//不再接收新任务 但会让已提交的任务继续执行 直到所有任务完成 新任务不再被接受
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();//尝试停止所有正在执行的任务 返回尚未执行的任务列表 会终端所有正在等待的任务
        }
    }

    @NotNull
    private static String getString(Task task, String directoryPath) {
        // 获取原始文件名
        String fileName = task.getDownloadLink().substring(task.getDownloadLink().lastIndexOf("/") + 1);
        // 获取文件扩展名和文件的基础名称
        String baseName;
        String extension = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex); // 获取"."后的扩展名
        } else {
            baseName = fileName; // 没有扩展名的情况
        }
        String fullPath = directoryPath + File.separator + fileName;
        File file = new File(fullPath);
        int i = 1;
        // 循环判断文件是否已经存在
        while (file.exists()) {
            // 检查 baseName 中是否已经包含类似 (n) 的后缀，并移除这个后缀
            if (baseName.matches(".*\\(\\d+\\)$")) {
                baseName = baseName.substring(0, baseName.lastIndexOf("("));
            }
            // 生成新的文件名，递增数字
            fileName = baseName + "(" + i + ")" + extension;
            fullPath = directoryPath + File.separator + fileName;
            file = new File(fullPath);
            i++;
        }
        return fullPath;
    }
    @Override
    public SseEmitter getEmitter(String taskId) {
        if (emitters.containsKey(taskId)) {
            return emitters.get(taskId);
        }
        return null;
    }
    public int sliceSize(Long fileSize) {
        if (fileSize <= 100 * 1024 * 1024) {//小于100MB
            return 1024 * 1024;//1MB
        } else if (fileSize <= 1024 * 1024 * 1024) {//小于1GB
            return 10 * 1024 * 1024;//10MB
        } else {//大于1GB
            return 50 * 1024 * 1024;//50MB;
        }
    }
    private String saveFilePath(String directoryPath, String destinationPath) throws IOException {
        File file = new File(destinationPath);
        File file1 = new File(directoryPath, file.getName());
        return file1.getAbsolutePath();
    }
}
