package net.zxmushroom63.plugins;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

import net.lax1dude.eaglercraft.v1_8.log4j.LogManager;
import net.lax1dude.eaglercraft.v1_8.log4j.Logger;
import net.minecraft.client.Minecraft;

import java.lang.Class;
import java.lang.String;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PluginAPI {
    private Minecraft mc;
    private static final Logger logger = LogManager.getLogger();
    private Map<String, Integer> globalsMap = new HashMap<String, Integer>();
    private Map<Integer, String> reverseGlobalsMap = new HashMap<Integer, String>();

    @JSBody(params = {}, script = "var PluginAPI = {};\r\n" + //
            "PluginAPI.events = {};\r\n" + //
            "PluginAPI.events.types = [];\r\n" + //
            "PluginAPI.events.listeners = {};\r\n" + //
            "PluginAPI.addEventListener = function addEventListener(name, callback) {\r\n" + //
            "  if (PluginAPI.events.types.includes(name)) {\r\n" + //
            "    if (!Array.isArray(PluginAPI.events.listeners[name])) {\r\n" + //
            "      PluginAPI.events.listeners[name] = [];\r\n" + //
            "    }\r\n" + //
            "    PluginAPI.events.listeners[name].push(callback);\r\n" + //
            "    console.log(\"Added new event listener.\");\r\n" + //
            "  } else {\r\n" + //
            "    throw new Error(\"This event does not exist!\");\r\n" + //
            "  }\r\n" + //
            "};\r\n" + //
            "PluginAPI.events.newEvent = function newEvent(name) {\r\n" + //
            "  PluginAPI.events.types.push(name);\r\n" + //
            "};\r\n" + //
            "PluginAPI.events.callEvent = function callEvent(name, data) {\r\n" + //
            "  if (\r\n" + //
            "    !PluginAPI.events.types.includes(name) ||\r\n" + //
            "    !Array.isArray(PluginAPI.events.listeners[name])\r\n" + //
            "  ) {\r\n" + //
            "    return;\r\n" + //
            "  }\r\n" + //
            "  PluginAPI.events.listeners[name].forEach((func) => {\r\n" + //
            "    func(data);\r\n" + //
            "  });\r\n" + //
            "};\r\n" + //
            "PluginAPI.updateComponent = function updateComponent(component) {\r\n" + //
            "  if (\r\n" + //
            "    typeof component !== \"string\" ||\r\n" + //
            "    PluginAPI[component] === null ||\r\n" + //
            "    PluginAPI[component] === undefined\r\n" + //
            "  ) {\r\n" + //
            "    return;\r\n" + //
            "  }\r\n" + //
            "  if (!PluginAPI.globals || !PluginAPI.globals.onGlobalsUpdate) {\r\n" + //
            "    return;\r\n" + //
            "  }\r\n" + //
            "  PluginAPI.globals.onGlobalUpdate(component);\r\n" + //
            "};\r\n" + //
            "window.PluginAPI = PluginAPI;")
    private static native void init();

    @JSBody(params = { "name" }, script = "PluginAPI.events.newEvent(name);")
    private static native void newEvent(String name);

    @JSBody(params = { "name", "data" }, script = "PluginAPI.events.callEvent(name, data); return data;")
    public static native BaseData callEvent(String name, BaseData data);

    @JSBody(params = { "name", "data" }, script = "PluginAPI[name]=data;")
    public static native void setGlobal(String name, BaseData data);

    @JSBody(params = { "name" }, script = "return PluginAPI[name];")
    public static native BaseData getGlobal(String name);

    public void onGlobalUpdated(String global) {
        logger.info("Global update request: "+global);
        BaseData data = getGlobal(global);
        if (data == null) {
            return;
        }
        switch (global) {
            case "player":
                mc.thePlayer.loadPluginData(data);
        }
    }

    public PluginAPI(Minecraft mcIn) {
        this.mc = mcIn;
        init();
        newEvent("sendchatmessage");
        newEvent("key");
        newEvent("update");
        newEvent("postmotionupdate");
        newEvent("premotionupdate");

        globalsFunctor(this);
    }

    static void globalsFunctor(PluginAPI pluginAPI) {
        GlobalsListener.provideCallback((String name)->{
            pluginAPI.onGlobalUpdated(name);
        });
    }
}