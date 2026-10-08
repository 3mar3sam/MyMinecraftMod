package com.example.muslimmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyInit {
    public static final KeyMapping PARRY_KEY = new KeyMapping(
        "key.muslimmod.parry",
        InputConstants.Type.MOUSE,
        GLFW.GLFW_MOUSE_BUTTON_4,
        "key.categories.muslimmod"
    );
}
