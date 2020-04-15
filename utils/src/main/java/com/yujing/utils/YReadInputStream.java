package com.yujing.utils;

import android.util.Log;

import com.yujing.contract.YListener1;

import java.io.InputStream;

/**
 * 读取InputStream
 *
 * @author yujing 2020年1月6日17:22:15
 */
@SuppressWarnings("unused")
public class YReadInputStream {
    private static final String TAG = "YRead";
    private static boolean showLog = false;
    private InputStream inputStream;
    private YListener1<byte[]> readListener;
    private ReadThread readThread;
    private final int loopWaitTime = 1;//循环等待时间1毫秒
    private boolean autoPackage = true;//自动组包
    private int packageTime = 1;//组包时间差，毫秒
    private int readLength = -1;//读取长度
    private int readTimeout = -1;//读取超时时间

    public YReadInputStream() {
    }

    public YReadInputStream(InputStream inputStream, YListener1<byte[]> readListener) {
        this.inputStream = inputStream;
        this.readListener = readListener;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setReadListener(YListener1<byte[]> readListener) {
        this.readListener = readListener;
    }

    //开始读取
    public void start() {
        readThread = new ReadThread();
        readThread.start();
    }

    //停止
    public void stop() {
        if (readThread != null) {
            readThread.interrupt();
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            try {
                log("开启一个读取线程");
                while (!Thread.currentThread().isInterrupted()) {
                    int available = inputStream.available();// 可读取多少字节内容
                    if (available == 0) {//如果可读取消息为0，那么久休息loopWaitTime毫秒。防止InputStream.read阻塞
                        try {
                            Thread.sleep(loopWaitTime);
                        } catch (Exception e) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                    YBytes yBytes;
                    if (!autoPackage && readTimeout > 0 && readLength > 0) {
                        //知道长度时，组包时间间隔意义不大
                        yBytes = read(inputStream, 1, readTimeout, readLength);
                    } else {
                        yBytes = read(inputStream, packageTime);
                    }
                    if (readListener != null) {
                        readListener.value(yBytes.getBytes());
                    }
                }
            } catch (Exception e) {
                log("读取线程崩溃", e);
            } finally {
                log("关闭一个读取线程");
            }
        }
    }

    private static void log(String string) {
        if (showLog) Log.i(TAG, string);
    }

    private static void log(String string, Exception e) {
        if (showLog) Log.e(TAG, string, e);
    }

    public static boolean isShowLog() {
        return showLog;
    }

    public static void setShowLog(boolean showLog) {
        YReadInputStream.showLog = showLog;
    }

    public int getPackageTime() {
        return packageTime;
    }

    public void setPackageTime(int packageTime) {
        this.packageTime = packageTime;
    }

    public void setLengthAndTimeout(int readLength, int readTimeout) {
        this.readLength = readLength;
        this.readTimeout = readTimeout;
    }

    public boolean isAutoPackage() {
        return autoPackage;
    }

    public void setAutoPackage(boolean autoPackage) {
        this.autoPackage = autoPackage;
    }

    //★★★★★★★★★★★★★★★★★★★★★★★★★★★★★读流操作★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★

    /**
     * 读取InputStream
     *
     * @param mInputStream     输入流
     * @param groupPackageTime 每次组包时间间隔
     * @return YBytes
     */
    public static YBytes read(final InputStream mInputStream, final int groupPackageTime) {
        final YBytes bytes = new YBytes();
        final long startTime = System.currentTimeMillis();
        //方法内部类，读取线程
        class MReadThread extends Thread {
            @Override
            public void run() {
                try {
                    int i = 0;//第几次组包
                    int available = mInputStream.available();// 可读取多少字节内容
                    int packageTime = 0;//每次组包时间间隔
                    while (!Thread.currentThread().isInterrupted()) {
                        i++;
                        //再读取一次
                        byte[] newBytes = new byte[1024];
                        int newSize = mInputStream.read(newBytes, 0, available);
                        if (newSize > 0) {
                            bytes.addByte(newBytes, newSize);
                            log("第" + i + "次组包后长度：" + bytes.getBytes().length + "，\t组包间隔：" + (packageTime) + "，\t最大间隔：" + (groupPackageTime) + "ms，\t已耗时：" + (System.currentTimeMillis() - startTime));
                        }
                        Thread.sleep(1);//每次组包间隔，毫秒
                        available = mInputStream.available();// 可读取多少字节内容
                        packageTime = 1;//组包时间间隔1ms
                        //如果读取长度为0，那么休息1毫秒继续读取，如果在groupPackageTime时间内都没有数据，那么就退出循环
                        if (available == 0) {
                            for (int j = 0; j <= groupPackageTime; j++) {
                                Thread.sleep(1);//每次组包间隔，毫秒
                                packageTime++;//组包时间间隔+1ms
                                available = mInputStream.available();// 可读取多少字节内容
                                if (available != 0) break;//如果读取到数据立即关闭循环
                            }
                        }
                        if (packageTime > groupPackageTime) break;//如果组包countLength0次后，大于设置的时间就退出读取
                    }
                } catch (InterruptedException e) {
                    interrupt();
                } catch (Exception e) {
                    log("读取线程异常", e);
                    interrupt();
                } finally {
                    log("读取线程关闭");
                    synchronized (bytes) {
                        bytes.notify();
                    }
                }
            }
        }
        //开个线程来读取
        final MReadThread mReadThread = new MReadThread();
        mReadThread.start();
        try {
            //同步锁
            synchronized (bytes) {
                bytes.wait();
            }
        } catch (Exception e) {
            mReadThread.interrupt();
            //Thread.currentThread().interrupt();
            log("同步锁被中断");
        }
        log(("读取完毕"));
        return bytes;
    }

    /**
     * 指定时间内读取指定长度的InputStream
     *
     * @param mInputStream     输入流
     * @param groupPackageTime 每次组包时间间隔
     * @param readTimeOut      超时时间
     * @param readLength       读取长度
     * @return YBytes
     */
    public static YBytes read(final InputStream mInputStream, final int groupPackageTime, final int readTimeOut, final int readLength) {
        final YBytes bytes = new YBytes();
        final long startTime = System.currentTimeMillis();
        //方法内部类，读取线程
        class MReadThread extends Thread {
            private boolean timeOut = true;

            private boolean isTimeOut() {
                return timeOut;
            }

            @Override
            public void run() {
                try {
                    int i = 0;//第几次组包
                    while (!Thread.currentThread().isInterrupted() && bytes.getBytes().length < readLength && i * groupPackageTime < readTimeOut) {
                        i++;
                        int available = mInputStream.available();// 可读取多少字节内容
                        if (available == 0) {//如果可读取消息为0，那么久休息loopWaitTime毫秒。防止InputStream.read阻塞
                            try {
                                Thread.sleep(groupPackageTime);
                            } catch (Exception e) {
                                Thread.currentThread().interrupt();
                            }
                            continue;
                        }
                        Thread.sleep(groupPackageTime);//每次组包间隔，毫秒
                        //再读取一次
                        byte[] newBytes = new byte[readLength < 1024 ? 1024 : readLength];
                        int newSize = mInputStream.read(newBytes, 0, available);
                        if (newSize > 0) {
                            bytes.addByte(newBytes, newSize);
                            log("第" + i + "次组包后长度：" + bytes.getBytes().length + "，\t目标长度：" + readLength + "，\t已耗时：" + (System.currentTimeMillis() - startTime) + "ms，\t超时时间：" + readTimeOut + "ms");
                        }
                    }
                    timeOut = false;
                } catch (InterruptedException e) {
                    interrupt();
                } catch (Exception e) {
                    log("读取线程异常", e);
                    interrupt();
                } finally {
                    log("读取线程关闭");
                    synchronized (bytes) {
                        bytes.notify();
                    }
                }
            }
        }
        //方法内部类，终止线程
        class StopReadThread extends Thread {
            @Override
            public void run() {
                try {
                    Thread.sleep(readTimeOut);
                } catch (InterruptedException e) {
                    interrupt();
                }
                if (!isInterrupted()) {
                    log("已超时：" + readTimeOut + "ms");
                    synchronized (bytes) {
                        bytes.notify();
                    }
                }
                log("超时线程关闭");
            }
        }

        //开个线程来读取
        final MReadThread mReadThread = new MReadThread();
        mReadThread.start();
        //开个线程来终止
        final StopReadThread stopReadThread = new StopReadThread();
        stopReadThread.start();
        try {
            //同步锁
            synchronized (bytes) {
                bytes.wait();
            }
        } catch (Exception e) {
            mReadThread.interrupt();
            stopReadThread.interrupt();
            //Thread.currentThread().interrupt();
            log("同步锁被中断");
        }
        log((mReadThread.isTimeOut() ? "读取超时！" : "读取完毕"));
        //释放这两个线程
        if (!mReadThread.isInterrupted())
            mReadThread.interrupt();
        if (!stopReadThread.isInterrupted())
            stopReadThread.interrupt();
        return bytes;
    }
}
