package com.palechip.hudpixelmod.extended.onlinefriends

import com.palechip.hudpixelmod.api.interaction.ApiQueueEntryBuilder
import com.palechip.hudpixelmod.api.interaction.callbacks.FriendResponseCallback
import com.palechip.hudpixelmod.config.GeneralConfigSettings
import com.palechip.hudpixelmod.extended.HudPixelExtended
import com.palechip.hudpixelmod.extended.HudPixelExtendedEventHandler.registerIEvent
import com.palechip.hudpixelmod.extended.data.player.IPlayerLoadedCallback
import com.palechip.hudpixelmod.extended.data.player.PlayerDatabase
import com.palechip.hudpixelmod.extended.data.player.PlayerFactory
import com.palechip.hudpixelmod.extended.util.IEventHandler
import com.palechip.hudpixelmod.extended.util.LoggerHelper.logInfo
import com.palechip.hudpixelmod.extended.util.LoggerHelper.logWarn
import com.palechip.hudpixelmod.extended.util.McColorHelper
import com.palechip.hudpixelmod.util.plus
import net.hypixel.api.reply.FriendsReply
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.lang.System.currentTimeMillis
import java.util.*

/* **********************************************************************************************************************
 * HudPixelReloaded - License
 * <p>
 * The repository contains parts of Minecraft Forge and its dependencies. These parts have their licenses
 * under forge-docs/. These parts can be downloaded at files.minecraftforge.net.This project contains a
 * unofficial copy of pictures from the official Hypixel website. All copyright is held by the creator!
 * Parts of the code are based upon the Hypixel Public API. These parts are all in src/main/java/net/hypixel/api and
 * subdirectories and have a special copyright header. Unfortunately they are missing a license but they are obviously
 * intended for usage in this kind of application. By default, all rights are reserved.
 * The original version of the HudPixel Mod is made by palechip and published under the MIT license.
 * The majority of code left from palechip's creations is the component implementation.The ported version to
 * Minecraft 1.8.9 and up HudPixel Reloaded is made by PixelModders/Eladkay and also published under the MIT license
 * (to be changed to the new license as detailed below in the next minor update).
 * <p>
 * For the rest of the code and for the build the following license applies:
 * <p>
 * # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
 * #  HudPixel by PixelModders, Eladkay & unaussprechlich is licensed under a Creative Commons         #
 * #  Attribution-NonCommercial-ShareAlike 4.0 International License with the following restrictions.  #
 * #  Based on a work at HudPixelExtended & HudPixel.                                                  #
 * # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
 * <p>
 * Restrictions:
 * <p>
 * The authors are allowed to change the license at their desire. This license is void for members of PixelModders and
 * to unaussprechlich, except for clause 3. The licensor cannot revoke these freedoms in most cases, as long as you follow
 * the following license terms and the license terms given by the listed above Creative Commons License, however in extreme
 * cases the authors reserve the right to revoke all rights for usage of the codebase.
 * <p>
 * 1. PixelModders, Eladkay & unaussprechlich are the authors of this licensed material. GitHub contributors are NOT
 * considered authors, neither are members of the HudHelper program. GitHub contributers still hold the rights for their
 * code, but only when it is used separately from HudPixel and any license header must indicate that.
 * 2. You shall not claim ownership over this project and repost it in any case, without written permission from at least
 * two of the authors.
 * 3. You shall not make money with the provided material. This project is 100% non commercial and will always stay that
 * way. This clause is the only one remaining, should the rest of the license be revoked. The only exception to this
 * clause is completely cosmetic features. Only the authors may sell cosmetic features for the mod.
 * 4. Every single contibutor owns copyright over his contributed code when separated from HudPixel. When it's part of
 * HudPixel, it is only governed by this license, and any copyright header must indicate that. After the contributed
 * code is merged to the release branch you cannot revoke the given freedoms by this license.
 * 5. If your own project contains a part of the licensed material you have to give the authors full access to all project
 * related files.
 * 6. You shall not act against the will of the authors regarding anything related to the mod or its codebase. The authors
 * reserve the right to take down any infringing project.
 **********************************************************************************************************************/

@SideOnly(Side.CLIENT)
class OnlineFriendsLoader : FriendResponseCallback, IEventHandler, IPlayerLoadedCallback {

    init {
        setupLoader()
    }

    fun setupLoader() {
        registerIEvent(this)
        requestFriends(true)
    }

    private fun requestFriends(forceRequest: Boolean?) {
        if (GeneralConfigSettings.useAPI && OnlineFriendManager.enabled) {
            // isHypixelNetwork if enough time has past
            if (currentTimeMillis() > lastRequest + REQUEST_COOLDOWN || forceRequest!!) {
                // save the time of the request
                lastRequest = currentTimeMillis()
                // tell the queue that we need boosters
                ApiQueueEntryBuilder.newInstance().friendsRequestByUUID(HudPixelExtended.UUID).setCallback(this).create()
            }
        }
    }

    override fun onFriendResponse(friendShips: List<FriendsReply.FriendShip>?) {
        if (friendShips == null) {
            logWarn("[OnlineFriends][APIloader]: The api answered the request with NULL!")
            return
        }
        logInfo("[OnlineFriends][APIloader]: The API answered with a total of " + friendShips.size + " friends! I will request all the Names now.")
        friendShips.forEach( { this.checkFriend(it) })
        isApiLoaded = true
    }

    fun checkFriend(f: FriendsReply.FriendShip) {
        if (f.uuidSender.toString() == HudPixelExtended.UUID.toString())
            PlayerFactory(f.uuidReceiver, this)
        else
            PlayerFactory(f.uuidSender, this)
    }

    override fun onPlayerLoadedCallback(uuid: UUID) {
        for (s in allreadyStoredUUID)
            if (s === uuid)
                return
        allreadyStoredUUID.add(uuid)
        allreadyStored.add(PlayerDatabase.getPlayerByUUID(uuid)?.name)
        OnlineFriendManager.addFriend(OnlineFriend(uuid, McColorHelper.GRAY + "Not loaded yet!"))
    }

    companion object {

        private val REQUEST_COOLDOWN = 20 * 60 * 1000 // = 30min
        private var lastRequest: Long = 0
        val allreadyStored = ArrayList<String?>()
        private val allreadyStoredUUID = ArrayList<UUID>()
        var isApiLoaded = false
            private set
    }
}
