package com.pepej.prison;

import tech.mcprison.prison.modules.Module;

public class PrisonItems extends Module {
    /**
     * Initialize your module.
     *
     * @param name    The name of the module.
     * @param version The version of the module.
     * @param target  The API level to target.
     */
    public PrisonItems(String name, String version, int target) {
        super(name, version, target);
    }

    @Override
    public String getBaseCommands() {
        return "/items";
    }

    @Override
    public void enable() {

    }

    @Override
    public void deferredStartup() {

    }

    @Override
    public void disable() {

    }
}
