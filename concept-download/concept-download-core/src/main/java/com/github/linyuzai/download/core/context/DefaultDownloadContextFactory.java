package com.github.linyuzai.download.core.context;

import com.github.linyuzai.download.core.options.DownloadOptions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * {@link DownloadContextFactory} 的默认实现。
 */
@Getter
@RequiredArgsConstructor
public class DefaultDownloadContextFactory implements DownloadContextFactory {

    /**
     * 初始化器
     */
    @NonNull
    private final List<DownloadContextInitializer> initializers;

    /**
     * 销毁器
     */
    @NonNull
    private final List<DownloadContextDestroyer> destroyers;

    /**
     * 创建一个 {@link DefaultDownloadContext}。
     * 使用 {@link UUID} 生成唯一ID。
     *
     * @param options {@link DownloadOptions}
     * @return {@link DefaultDownloadContext}
     */
    @Override
    public DownloadContext create(DownloadOptions options) {
        DefaultDownloadContext context = new DefaultDownloadContext(UUID.randomUUID().toString(), options);
        context.setInitializers(initializers);
        context.setDestroyers(destroyers);
        return context;
    }
}
