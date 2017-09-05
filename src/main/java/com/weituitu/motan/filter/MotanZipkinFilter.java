package com.weituitu.motan.filter;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.weibo.api.motan.core.extension.Activation;
import com.weibo.api.motan.core.extension.SpiMeta;
import com.weibo.api.motan.filter.Filter;
import com.weibo.api.motan.rpc.Caller;
import com.weibo.api.motan.rpc.Provider;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;
import com.weituitu.motan.BraveContextAware;


/**
 * @描述: 新浪微博motan框架zipkin扩展
 * @作者:liuguozhu
 * @创建:2017/9/1-下午2:48
 * @版本:v1.0
 */
@SpiMeta(name = "zipkinfilter")
@Activation(sequence = 30)
public class MotanZipkinFilter implements Filter {
    public static final String ZIPKIN_TRACING_BEAN_NAME = "zipkin-tracing";
    private Tracing tracing;
    private Tracer tracer;
    private TraceContext.Injector<Request> injector;

    private TraceContext.Extractor<Request> extractor;
    /**
     * 标记初始化，提高性能
     */
    private boolean init;

    public MotanZipkinFilter() {


    }

    public Response filter(Caller<?> caller, Request request) {
        if (tracing == null || tracer == null) {
            if (init) {
                return caller.call(request);
            }
            init = true;
            if (BraveContextAware.getApplicationContext().containsBean(ZIPKIN_TRACING_BEAN_NAME)) {
                tracing = (Tracing) BraveContextAware.getApplicationContext().getBean(ZIPKIN_TRACING_BEAN_NAME);
                if (tracing != null) {
                    tracer = tracing.tracer();
                    extractor = tracing.propagationFactory().create(MotanKeyFactory.INSTANCE)
                            .extractor(new Propagation.Getter<Request, String>() {
                                @Override
                                public String get(Request carrier, String key) {
                                    return carrier.getAttachments().get(key);
                                }

                            });

                    injector = tracing.propagationFactory().create(MotanKeyFactory.INSTANCE)
                            .injector(new Propagation.Setter<Request, String>() {
                                @Override
                                public void put(Request carrier, String key, String value) {
                                    carrier.getAttachments().put(key, value);
                                }
                            });
                }

            }
        }
        if (tracing == null || tracer == null) {
            return caller.call(request);
        }
        Response response = null;
        if (caller instanceof Provider) {
            System.out.println("server response " + request.getMethodName());
            TraceContextOrSamplingFlags contextOrFlags = extractor.extract(request);
            Span span = contextOrFlags.context() != null
                    ? tracer.joinSpan(contextOrFlags.context())
                    : tracer.newTrace(contextOrFlags.samplingFlags());
            span.kind(Span.Kind.SERVER).name(request.getMethodName()).start();
            try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
                response = caller.call(request);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                span.finish();
            }

        } else {
            System.out.println("client call  " + request.getMethodName());
            Span span = tracer.nextSpan();
            tracer = tracing.tracer();
            try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
                injector.inject(span.context(), request);
                span.kind(Span.Kind.CLIENT).name(request.getMethodName()).start();
                response = caller.call(request);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                span.finish();
            }

        }
        return response;


    }


}
