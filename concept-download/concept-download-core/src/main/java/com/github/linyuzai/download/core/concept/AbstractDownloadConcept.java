package com.github.linyuzai.download.core.concept;

import com.github.linyuzai.download.core.context.DownloadContext;
import com.github.linyuzai.download.core.context.DownloadContextFactory;
import com.github.linyuzai.download.core.event.DownloadCompletedEvent;
import com.github.linyuzai.download.core.event.DownloadEventPublisher;
import com.github.linyuzai.download.core.event.DownloadStartedEvent;
import com.github.linyuzai.download.core.handler.DownloadHandler;
import com.github.linyuzai.download.core.options.DownloadOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public abstract class AbstractDownloadConcept implements DownloadConcept {

    /**
     * 上下文工厂
     */
    private final DownloadContextFactory contextFactory;

    /**
     * 处理器
     */
    private final List<DownloadHandler> handlers;

    private final DownloadEventPublisher eventPublisher;

    @Override
    public Object download(DownloadOptions options) {
        //创建上下文
        DownloadContext context = contextFactory.create();
        context.set(DownloadOptions.class, options);
        context.set(DownloadEventPublisher.class, eventPublisher);
        eventPublisher.publish(new DownloadStartedEvent(context));
        List<DownloadHandler> filtered = handlers.stream()
                .filter(it -> it.support(context))
                .collect(Collectors.toList());
        //处理链
        return doDownload(context, filtered, () ->
                eventPublisher.publish(new DownloadCompletedEvent(context)));
    }

    protected abstract Object doDownload(DownloadContext context, List<DownloadHandler> handlers, Runnable onComplete);
}
