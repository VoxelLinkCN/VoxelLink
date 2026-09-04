package dev.voxellink.util.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.HostAccess;
import org.jetbrains.annotations.Nullable;

@Getter(onMethod_ = {@HostAccess.Export})
@RequiredArgsConstructor
public class HttpResult {
    private final int statusCode;
    private final @Nullable String responseContent;
}
