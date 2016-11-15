package com.palechip.hudpixelmod.api.interaction;

import com.palechip.hudpixelmod.api.interaction.callbacks.ApiCallback;
import com.palechip.hudpixelmod.api.interaction.callbacks.BoosterResponseCallback;
import com.palechip.hudpixelmod.api.interaction.callbacks.FriendResponseCallback;
import com.palechip.hudpixelmod.extended.util.LoggerHelper;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.reply.BoostersReply;
import net.hypixel.api.reply.FriendsReply;
import net.hypixel.api.request.Request;
import net.hypixel.api.util.Callback;

/***********************************************************************************************************************
 HudPixelReloaded - License

 The repository contains parts of Minecraft Forge and its dependencies. These parts have their licenses 
 under forge-docs/. These parts can be downloaded at files.minecraftforge.net.This project contains a 
 unofficial copy of pictures from the official Hypixel website. All copyright is held by the creator!
 Parts of the code are based upon the Hypixel Public API. These parts are all in src/main/java/net/hypixel/api and 
 subdirectories and have a special copyright header. Unfortunately they are missing a license but they are obviously 
 intended for usage in this kind of application. By default, all rights are reserved.
 The original version of the HudPixel Mod is made by palechip and published under the MIT license. 
 The majority of code left from palechip's creations is the component implementation.The ported version to 
 Minecraft 1.8.9 and up HudPixel Reloaded is made by PixelModders/Eladkay and also published under the MIT license 
 (to be changed to the new license as detailed below in the next minor update).

 For the rest of the code and for the build the following license applies:

 # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
 #  HudPixel by PixelModders, Eladkay & unaussprechlich is licensed under a Creative Commons         #
 #  Attribution-NonCommercial-ShareAlike 4.0 International License with the following restrictions.  #
 #  Based on a work at HudPixelExtended & HudPixel.                                                  #
 # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # 

 Restrictions:

 The authors are allowed to change the license at their desire. This license is void for members of PixelModders and 
 to unaussprechlich, except for clause 3. The licensor cannot revoke these freedoms in most cases, as long as you follow 
 the following license terms and the license terms given by the listed above Creative Commons License, however in extreme 
 cases the authors reserve the right to revoke all rights for usage of the codebase.

 1. PixelModders, Eladkay & unaussprechlich are the authors of this licensed material. GitHub contributors are NOT 
 considered authors, neither are members of the HudHelper program. GitHub contributers still hold the rights for their 
 code, but only when it is used separately from HudPixel and any license header must indicate that.
 2. You shall not claim ownership over this project and repost it in any case, without written permission from at least 
 two of the authors.
 3. You shall not make money with the provided material. This project is 100% non commercial and will always stay that 
 way. This clause is the only one remaining, should the rest of the license be revoked. The only exception to this 
 clause is completely cosmetic features. Only the authors may sell cosmetic features for the mod.
 4. Every single contibutor owns copyright over his contributed code when separated from HudPixel. When it's part of 
 HudPixel, it is only governed by this license, and any copyright header must indicate that. After the contributed 
 code is merged to the release branch you cannot revoke the given freedoms by this license.
 5. If your own project contains a part of the licensed material you have to give the authors full access to all project 
 related files.
 6. You shall not act against the will of the authors regarding anything related to the mod or its codebase. The authors 
 reserve the right to take down any infringing project.
 **********************************************************************************************************************/
public class ApiQueueEntry{

    private static final String debugInfo = "[API][QueueEntry]";

    private long creationTime;

    private ApiCallback apiCallback;
    private Request request;

    public ApiQueueEntry(Request request, ApiCallback apiCallback){
        this.request = request;
        this.apiCallback = apiCallback;
        this.creationTime = System.currentTimeMillis();
    }

    public void execute(){
        LoggerHelper.logInfo(debugInfo + "Making a request: " + request.getURL(HypixelAPI.getInstance()));

        switch (request.getRequestType()){
            case BOOSTERS: executeBoosterRequest(); break;
            case FRIENDS:  executeFriendRequest();  break;
        }

        ApiQueue.removeCurrentEntry();
    }

    private void executeFriendRequest(){
        HypixelAPI.getInstance().getAsync(request, new Callback<FriendsReply>(FriendsReply.class) {
            @Override
            public void callback(Throwable failCause, FriendsReply result) {
                if (failCause != null) failCause.printStackTrace();
                else ((FriendResponseCallback)apiCallback).onFriendResponse(result.getFriendShips());
                //HypixelAPI.getInstance().finish();
            }
        });
    }

    private void executeBoosterRequest(){
        HypixelAPI.getInstance().getAsync(request, new Callback<BoostersReply>(BoostersReply.class) {
            @Override
            public void callback(Throwable failCause, BoostersReply result) {
                System.out.println("Got a callback!");
                if (failCause != null) failCause.printStackTrace();
                else ((BoosterResponseCallback)apiCallback).onBoosterResponse(result.getBoosters());
                System.out.println(result);
                //HypixelAPI.getInstance().finish();
            }
        });
    }



}