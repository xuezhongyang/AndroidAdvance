package com.ldlywt.easyhttp;

import com.ldlywt.easyhttp.chain.CallServiceInterceptor;
import com.ldlywt.easyhttp.chain.ConnectionInterceptor;
import com.ldlywt.easyhttp.chain.HeadersInterceptor;
import com.ldlywt.easyhttp.chain.Interceptor;
import com.ldlywt.easyhttp.chain.InterceptorChain;
import com.ldlywt.easyhttp.chain.RetryInterceptor;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <pre>
 *     author : lex
 *     e-mail : ldlywt@163.com
 *     time   : 2018/08/21
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class Call {

    private boolean executed;
    private boolean canceled;
    private HttpClient mHttpClient;
    private Request mRequest;

    public Call(HttpClient httpClient, Request request) {
        mHttpClient = httpClient;
        mRequest = request;
    }

    public Request getRequest() {
        return mRequest;
    }

    public boolean isExecuted() {
        return executed;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public String getHost() {
        return mRequest.getHttpUrl().getHost();
    }

    public HttpClient getHttpClient() {
        return mHttpClient;
    }

    public Response execute() throws IOException {
        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("Already Executed");
            }
            executed = true;
        }
        try {
            mHttpClient.getDispatcher().executed(this);
            Response result = getResponseWithInterceptorChain();
            if (result == null) {
                throw new IOException("Canceled");
            }
            return result;
        } finally {
            mHttpClient.getDispatcher().executed(this);
        }
    }

    /**
     * 获取返回
     *
     * @return
     * @throws IOException
     */
    private Response getResponseWithInterceptorChain() throws IOException {
        ArrayList<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(mHttpClient.getInterceptorList());
        interceptors.add(new RetryInterceptor());
        interceptors.add(new HeadersInterceptor());
        interceptors.add(new ConnectionInterceptor());
        interceptors.add(new CallServiceInterceptor());
        InterceptorChain interceptorChain = new InterceptorChain(interceptors, 0, this, null);
        return interceptorChain.proceed();
    }

    /**
     * 将Call对象放到调度器里面去执行，如果已经加过了，就不能加了
     *
     * @param callback
     */
    public void enqueue(Callback callback) {
        synchronized (this) {
            if (executed) {
                throw new IllegalStateException("This Call Already Executed!");
            }
            executed = true;
        }
        mHttpClient.getDispatcher().enqueue(new AsyncCall(callback));
    }

    final class AsyncCall implements Runnable {

        private Callback callback;

        public AsyncCall(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            boolean signalledCallback = false;
            try {
                Response response = getResponseWithInterceptorChain();
                if (canceled) {
                    signalledCallback = true;
                    //回调，注意这里回调是在线程池中，而不是想当然的主线程回调
                    callback.onFailure(Call.this, new IOException("this task had canceled"));
                } else {
                    signalledCallback = true;
                    callback.onResponse(Call.this, response);
                }
            } catch (IOException e) {
                if (!signalledCallback) {
                    callback.onFailure(Call.this, e);
                }
            } finally {
                //将这个任务从调度器移除
                //当任务执行完成后，无论成功与否都会调用dispatcher.finished方法，通知分发器相关任务已结束
                mHttpClient.getDispatcher().finished(this);
            }
        }

        public String getHost() {
            return mRequest.getHttpUrl().getHost();
        }
    }
}
