package com.example.examplemod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyInit {
    public static final KeyMapping PARRY_KEY = new KeyMapping(
        "key.examplemod.parry",
        InputConstants.Type.MOUSE,
        GLFW.GLFW_MOUSE_BUTTON_4,
        "key.categories.examplemod"
    );
}
