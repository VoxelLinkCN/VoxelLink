package dev.voxellink.bot;

import dev.voxellink.api.action.get.*;
import dev.voxellink.api.action.set.SendPrivateMessage;
import dev.voxellink.api.action.set.*;
import dev.voxellink.api.event.NEvent;
import dev.voxellink.api.event.message.GroupMessageEvent;
import dev.voxellink.api.event.message.PrivateMessageEvent;
import dev.voxellink.api.event.notice.*;
import dev.voxellink.api.event.request.FriendRequestEvent;
import dev.voxellink.api.event.request.GroupRequestEvent;
import dev.voxellink.api.event.request.GroupRequestType;
import dev.voxellink.api.listener.VBotEventHandler;
import dev.voxellink.api.listener.VBotListener;
import dev.voxellink.VoxelLink;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BotListener implements VBotListener {
    private final VoxelLink plugin;

    private final Map<Value, String> map = new LinkedHashMap<>();
    
    public BotListener(VoxelLink plugin) {
        this.plugin = plugin;
    }

    @HostAccess.Export
    public void sendGroupMessage(long groupId, String message) {
        boolean sent = false;
        for (dev.voxellink.api.client.VBotClient client : plugin.getBotProvider().getBot()) {
            if (client.isConnected()) {
                client.action(new SendGroupMessage(groupId, textMessageSegments(message)));
                sent = true;
            }
        }
        if (!sent) throw new IllegalStateException("No connected QQ bot client to send group message to " + groupId);
    }

    private static JSONArray textMessageSegments(String message) {
        return new JSONArray().put(new JSONObject().put("type", "text").put("data", new JSONObject().put("text", message)));
    }

    public void clearUuidContext(String uuid) {
        List<Value> toRemove = new ArrayList<>();
        for (Map.Entry<Value, String> entry : map.entrySet()) {
            if (entry.getKey().getContext().getBindings("js").getMember("__uuid__").asString().equals(uuid)) {
                toRemove.add(entry.getKey());
            }
        }
        for (Value value : toRemove) {
            map.remove(value);
        }
    }

    @HostAccess.Export
    public void sendPrivateMessage(long userId, String message) {
        boolean sent = false;
        for (dev.voxellink.api.client.VBotClient client : plugin.getBotProvider().getBot()) {
            if (client.isConnected()) {
                client.action(new SendPrivateMessage(userId, textMessageSegments(message)));
                sent = true;
            }
        }
        if (!sent) throw new IllegalStateException("No connected QQ bot client to send private message to " + userId);
    }

    @HostAccess.Export
    public void renameGroupMember(long groupId, long userId, String name) {
        plugin.getBotProvider().getBot().forEach(client -> {
            if (client.isConnected()) {
                client.action(new SetGroupCard(groupId, userId, name));
            }
        });
    }

    @HostAccess.Export
    public void muteGroupMember(long groupId, long userId, int duration) {
        plugin.getBotProvider().getBot().forEach(client -> {
            if (client.isConnected()) {
                client.action(new SetGroupBan(groupId, userId, duration));
            }
        });
    }

    @HostAccess.Export
    public void muteAllGroupMember(long groupId) {
        plugin.getBotProvider().getBot().forEach(client -> {
            if (client.isConnected()) {
                client.action(new SetGroupWholeBan(groupId, true));
            }
        });
    }

    @HostAccess.Export
    public void unMuteAllGroupMember(long groupId) {
        plugin.getBotProvider().getBot().forEach(client -> {
            if (client.isConnected()) {
                client.action(new SetGroupWholeBan(groupId, false));
            }
        });
    }

    @HostAccess.Export
    public void kickGroupMember(long groupId, long userId) {
        plugin.getBotProvider().getBot().forEach(client -> {
            if (client.isConnected()) {
                client.action(new SetGroupKick(groupId, userId));
            }
        });
    }

    @HostAccess.Export
    public void approveGroupRequest(String flag, String type) {
        plugin.getBotProvider().getBot().forEach(client -> {
            if (client.isConnected()) {
                client.action(new SetGroupAddRequest(flag, GroupRequestType.from(type)));
            }
        });
    }

    @HostAccess.Export
    public void rejectGroupRequest(String flag, String type) {
        plugin.getBotProvider().getBot().forEach(client -> {
            if (client.isConnected()) {
                client.action(new SetGroupAddRequest(flag, GroupRequestType.from(type), false));
            }
        });
    }

    @HostAccess.Export
    public void approveFriendRequest(String flag) {
        plugin.getBotProvider().getBot().forEach(client -> {
            if (client.isConnected()) {
                client.action(new SetFriendAddRequest(flag));
            }
        });
    }

    @HostAccess.Export
    public void rejectFriendRequest(String flag) {
        plugin.getBotProvider().getBot().forEach(client -> {
            if (client.isConnected()) {
                client.action(new SetFriendAddRequest(flag, false));
            }
        });
    }

    @HostAccess.Export
    public void setGroupSpecialTitle(long groupId, long userId, String title, long duration) {
        plugin.getBotProvider().getBot().forEach(client -> {
            if (client.isConnected()) {
                client.action(new SetGroupSpecialTitle(groupId, userId, title, duration));
            }
        });
    }

    @HostAccess.Export
    public void setGroupWholeBan(long groupId, boolean enable) {
        plugin.getBotProvider().getBot().forEach(client -> {
            if (client.isConnected()) {
                client.action(new SetGroupWholeBan(groupId, enable));
            }
        });
    }

    @HostAccess.Export
    public void recallMessage(long messageId) {
        plugin.getBotProvider().getBot().forEach(client -> {
            if (client.isConnected()) {
                client.action(new DeleteMessage(messageId));
            }
        });
    }

    @HostAccess.Export
    public void getGroupMemberInfo(long groupId, long userId, Value method) {
        if (method.canExecute()) {
            plugin.getBotProvider().getBot().forEach(client -> {
                if (client.isConnected()) {
                    client.action(new GetGroupMemberInfo(groupId, userId), method::execute);
                } else {
                    method.execute(new Object());
                }
            });
        }
    }

    @HostAccess.Export
    public void getGroupMemberList(long groupId, Value method) {
        if (method.canExecute()) {
            plugin.getBotProvider().getBot().forEach(client -> {
                if (client.isConnected()) {
                    client.action(new GetGroupMemberList(groupId), method::execute);
                } else method.execute(new ArrayList<>());
            });
        }
    }

    @HostAccess.Export
    public void getFriendList(Value method) {
        if (method.canExecute()) {
            plugin.getBotProvider().getBot().forEach(client -> {
                if (client.isConnected()) {
                    client.action(new GetFriendList(), method::execute);
                } else method.execute(new ArrayList<>());
            });
        }
    }

    @HostAccess.Export
    public void getGroupList(Value method) {
        if (method.canExecute()) {
            plugin.getBotProvider().getBot().forEach(client -> {
                if (client.isConnected()) {
                    client.action(new GetGroupList(), method::execute);
                } else method.execute(new ArrayList<>());
            });
        }
    }

    @HostAccess.Export
    public void getGroupInfo(long groupId, Value method) {
        if (method.canExecute()) {
            plugin.getBotProvider().getBot().forEach(client -> {
                if (client.isConnected()) {
                    client.action(new GetGroupInfo(groupId), method::execute);
                } else method.execute(new ArrayList<>());
            });
        }
    }

    @HostAccess.Export
    public void register(String eventName, Value method) {
        if (method.canExecute()) map.put(method, eventName);
    }

    public void reset() {
        map.clear();
    }

    private void fireEvent(String eventName, NEvent event) {
        try {
            for (Map.Entry<Value, String> entry : map.entrySet()) {
                if (entry.getValue().equals(eventName)) {
                    entry.getKey().execute(event);
                }
            }
        } catch (Exception e) {
            plugin.getNeoLogger().error("Failed to fire event", e);
        }
    }
    
    @VBotEventHandler
    private void onGroupMessage(GroupMessageEvent event) {
        if (plugin.getScriptProvider() == null) {
            plugin.getNeoLogger().warn("QQGroupMessageEvent rejected: script system is not loaded");
            fireEvent("GroupMessageEvent", event);
            return;
        }
        if (!isSelfMessage(event.getSenderId(), event.getSelfId())) {
            String raw = event.getRawMessage();
            String replyTarget = "qq:" + event.getGroupId();
            String senderId = String.valueOf(event.getSenderId());
            String bindAction = bindOrUnbindAction(raw);
            String eventName;
            Object context;
            if (bindAction != null) {
                eventName = "bind".equals(bindAction) ? "BindEvent" : "UnbindEvent";
                context = new dev.voxellink.script.InboundCommandContext(raw, senderId, replyTarget, "qq");
            } else if (isEnabledGroup(event.getGroupId()) && plugin.getDiscordService().looksLikeRemoteCommand(raw)) {
                eventName = "RemoteCommandEvent";
                context = new dev.voxellink.script.InboundCommandContext(raw, senderId, replyTarget, "qq");
            } else {
                eventName = "QQGroupMessageEvent";
                context = event;
            }
            dispatchAndExecute(eventName, context);
        }
        fireEvent("GroupMessageEvent", event);
    }

    /** Dispatches a script event and executes the returned actions off the WebSocket receive thread.
     *  The receive thread is also the thread that delivers pending action responses (nickname/channel
     *  lookups), so running the script synchronously here can deadlock the frame-processing loop and
     *  silently drop messages. Scheduling via submitAsync lets the receive thread return immediately. */
    private void dispatchAndExecute(String eventName, Object context) {
        dev.voxellink.script.ScriptProvider provider = plugin.getScriptProvider();
        if (provider == null) {
            plugin.getNeoLogger().warn(eventName + " rejected: script system is not loaded");
            return;
        }
        plugin.submitAsync(() -> {
            dev.voxellink.script.ScriptDispatchResult result = provider.dispatchBusiness(eventName, context);
            provider.executeBusinessActions(result);
        });
    }

    private boolean isEnabledGroup(long id) { for (String g : plugin.getGeneralConfig().getStringArray("bot.options.enable-groups")) if (String.valueOf(id).equals(g)) return true; return false; }

    /** True when a group message was sent by the bot's own QQ account, which would otherwise loop
     *  a Minecraft-to-QQ forward straight back into Minecraft. */
    static boolean isSelfMessage(long senderId, long selfId) { return senderId == selfId; }

    /** Extracts "bind"/"unbind" from a two-token "/bind <player>" or "bind <player>" message, else null. */
    public static String bindOrUnbindAction(String raw) {
        if (raw == null) return null;
        String[] parts = raw.trim().split("\\s+", 3);
        if (parts.length != 2) return null;
        String action = parts[0].startsWith("/") ? parts[0].substring(1) : parts[0];
        if (action.equalsIgnoreCase("bind")) return "bind";
        if (action.equalsIgnoreCase("unbind")) return "unbind";
        return null;
    }

    @VBotEventHandler
    private void onPrivateMessage(PrivateMessageEvent event) {
        String raw = event.getRawMessage();
        String bindAction = bindOrUnbindAction(raw);
        if (bindAction != null) {
            String senderId = String.valueOf(event.getSenderId());
            if (plugin.getScriptProvider() == null) {
                plugin.getNeoLogger().warn("BindEvent rejected: script system is not loaded");
            } else {
                String eventName = "bind".equals(bindAction) ? "BindEvent" : "UnbindEvent";
                Object context = new dev.voxellink.script.InboundCommandContext(
                        raw, senderId, "qq:private:" + senderId, "qq");
                dispatchAndExecute(eventName, context);
            }
        }
        fireEvent("PrivateMessageEvent", event);
    }

    @VBotEventHandler
    private void onFriendAdd(FriendAddEvent event) {
        fireEvent("FriendAddEvent", event);
    }

    @VBotEventHandler
    private void onGroupDecrease(GroupDecreaseEvent event) {
        fireEvent("GroupDecreaseEvent", event);
    }

    @VBotEventHandler
    private void onGroupIncrease(GroupIncreaseEvent event) {
        fireEvent("GroupIncreaseEvent", event);
    }

    @VBotEventHandler
    private void onPoke(PokeEvent event) {
        fireEvent("PokeEvent", event);
    }

    @VBotEventHandler
    private void onFriendRequest(FriendRequestEvent event) {
        fireEvent("FriendRequestEvent", event);
    }

    @VBotEventHandler
    private void onGroupRequest(GroupRequestEvent event) {
        fireEvent("GroupRequestEvent", event);
    }
}
