package dev.voxellink.util;

import dev.voxellink.script.Script;
import lombok.Data;
import org.graalvm.polyglot.Value;

@Data
public class ValueWithScript {
    private final Value value;
    private final Script script;
}
