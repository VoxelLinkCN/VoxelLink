package dev.voxellink.event;

import com.velocitypowered.api.event.ResultedEvent;
import dev.voxellink.game.event.LoginEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.graalvm.polyglot.HostAccess;

public class VelocityLoginEvent extends LoginEvent {
    private final com.velocitypowered.api.event.connection.LoginEvent loginEvent;

    public VelocityLoginEvent(com.velocitypowered.api.event.connection.LoginEvent loginEvent) {
        super(loginEvent.getPlayer().getUsername(), loginEvent.getPlayer().getUniqueId());
        this.loginEvent = loginEvent;
    }

    @HostAccess.Export
    @Override
    public void disallow(String reason) {
        loginEvent.setResult(ResultedEvent.ComponentResult.denied(LegacyComponentSerializer.legacySection().deserialize(reason)));
    }
}
