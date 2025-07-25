package com.github.hhhzzzsss.cbset;

import net.minecraft.world.level.block.entity.CommandBlockEntity;

public enum NamedMode {
    CHAIN(CommandBlockEntity.Mode.SEQUENCE, "chain"),
    REPEATING(CommandBlockEntity.Mode.AUTO, "repeating"),
    IMPULSE(CommandBlockEntity.Mode.REDSTONE, "impulse");

    private final CommandBlockEntity.Mode mode;
    private final String name;

    public CommandBlockEntity.Mode getMode() {
        return mode;
    }
    public String getName() {
        return name;
    }

    NamedMode(CommandBlockEntity.Mode mode, String name) {
        this.mode = mode;
        this.name = name;
    }

    public static NamedMode getByName(String name) {
        for (NamedMode mode : NamedMode.values()) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return null;
    }
}
