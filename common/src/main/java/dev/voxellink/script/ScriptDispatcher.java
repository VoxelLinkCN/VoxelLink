package dev.voxellink.script;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/** Thread-safe synchronous dispatcher. A timeout or exception is a hard rejection. */
public final class ScriptDispatcher {
    public interface Handler { ScriptDispatchResult handle(String event, Object context) throws Exception; }
    /** Receives dispatch failures. error == null means a timeout; otherwise it is the underlying failure. */
    public interface ErrorReporter { void report(String message, Throwable error); }
    private final CopyOnWriteArrayList<Handler> handlers = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final long timeoutMillis;
    private final ErrorReporter errorReporter;
    public ScriptDispatcher(long timeoutMillis) { this(timeoutMillis, null); }
    public ScriptDispatcher(long timeoutMillis, ErrorReporter errorReporter) {
        this.timeoutMillis = Math.max(1L, timeoutMillis);
        this.errorReporter = errorReporter;
    }
    public void register(Handler handler) { if (handler != null) handlers.addIfAbsent(handler); }
    public void unregister(Handler handler) { handlers.remove(handler); }
    /** Drops every registered handler without touching handlers bound to an already-closed script context. */
    public void clearHandlers() { handlers.clear(); }
    public ScriptDispatchResult dispatch(String event, Object context) {
        if (handlers.isEmpty()) return ScriptDispatchResult.rejected();
        for (Handler handler : handlers) {
            Future<ScriptDispatchResult> future = executor.submit(() -> handler.handle(event, context));
            try {
                ScriptDispatchResult result = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
                if (result != null && result.isHandled()) return result;
            } catch (TimeoutException timeout) {
                future.cancel(true);
                if (errorReporter != null) errorReporter.report(
                        "Script dispatch timed out after " + timeoutMillis + "ms for event '" + event
                                + "' — message dropped (check for blocking lookups inside the script)",
                        null);
                return ScriptDispatchResult.rejected();
            } catch (Exception error) {
                future.cancel(true);
                if (errorReporter != null) errorReporter.report(
                        "Script dispatch failed for event '" + event + "' — message dropped", error);
                return ScriptDispatchResult.rejected();
            }
        }
        return ScriptDispatchResult.unhandled();
    }
    public void close() { executor.shutdownNow(); handlers.clear(); }
}
