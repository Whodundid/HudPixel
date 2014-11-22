package com.palechip.hudpixelmod.api.interaction;

import java.util.UUID;

import com.palechip.hudpixelmod.HudPixelMod;

import net.hypixel.api.HypixelAPI;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.util.Callback;

public class PublicAPIConnector implements ApiKeyLoadedCallback{
    ApiKeyHandler keyHandler;
    boolean apiEnabled = false;
    HypixelAPI api;
    
    public PublicAPIConnector() {
        this.keyHandler = new ApiKeyHandler(this);
        this.api = HypixelAPI.getInstance();
    }

    public void onChatMessage(String textMessage) {
        this.keyHandler.onChatMessage(textMessage);
    }

    @Override
    public void ApiKeyLoaded(boolean failed, String key) {
        if(failed) {
            this.apiEnabled = false;
        } else {
            this.api.setApiKey(UUID.fromString(key));
            this.apiEnabled = true;
        }
    }
}