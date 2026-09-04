package dev.voxellink.adapter.executor;

import dev.voxellink.adapter.RemoteExecutor;
import org.graalvm.polyglot.HostAccess;

public class NativeExecutor implements RemoteExecutor {
    private final BukkitConsoleSender delegate = new BukkitConsoleSender();

    @HostAccess.Export
    @Override
    public boolean init() {
        return delegate.init();
    }

    @HostAccess.Export
    @Override
    public void execute(String command) {
        delegate.execute(command);
    }

    @HostAccess.Export
    @Override
    public String getResult() {
        return delegate.getResult();
    }
}
