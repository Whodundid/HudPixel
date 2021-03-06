/*
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
 */

package eladkay.hudpixel.modulargui.components;

import eladkay.hudpixel.GameDetector;
import eladkay.hudpixel.config.CCategory;
import eladkay.hudpixel.config.ConfigPropertyBoolean;
import eladkay.hudpixel.modulargui.SimpleHudPixelModularGuiProvider;
import eladkay.hudpixel.util.GameType;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.unaussprechlich.hudpixelextended.util.McColorHelper;

public class KillstreakTrackerModularGuiProvider extends SimpleHudPixelModularGuiProvider implements McColorHelper {
    public static final String CURRENT_KILLSTREAK_DISPLAY_TEXT = EnumChatFormatting.DARK_PURPLE + "Killstreak: ";
    @ConfigPropertyBoolean(category = CCategory.HUD, id = "killstreakTracker", comment = "The Killstreak Tracker", def = true)
    public static boolean enabled = false;
    private final String GREATEST_KILLSTREAK_DISPLAY_TEXT = EnumChatFormatting.LIGHT_PURPLE + "Best Killstreak: ";
    private int currentKillstreak;
    private int greatestKillstreak;
    private boolean showGreatest;

    @Override
    public boolean doesMatchForGame() {
        return GameDetector.doesGameTypeMatchWithCurrent(GameType.ANY_TNT) || GameDetector.doesGameTypeMatchWithCurrent(GameType.QUAKECRAFT) || GameDetector.doesGameTypeMatchWithCurrent(GameType.ANY_ARCADE);
    }

    @Override
    public void setupNewGame() {
        //reset
        this.currentKillstreak = 0;
        this.greatestKillstreak = 0;
        this.showGreatest = false;
    }

    @Override
    public void onGameEnd() {
        this.showGreatest = true;
    }

    @Override
    public void onChatMessage(String textMessage, String formattedMessage) {
        String username = FMLClientHandler.instance().getClient().getSession().getUsername();
        // Quake
        if (textMessage.contains(username + " gibbed ")) {
            this.addKill();
        } else if (textMessage.contains(" gibbed " + username)) {
            this.resetKillstreak();
        }
        // TNT Wizards
        else if (textMessage.contains("You killed ")) {
            this.addKill();
        } else if (textMessage.contains("You were killed by ")) {
            this.resetKillstreak();
        }
        // Dragon Wars
        else if (textMessage.contains(username + " killed ")) {
            this.addKill();
        } else if (textMessage.contains(" killed " + username)) {
            this.resetKillstreak();
        }
        // Bounty Hunters
        else if (textMessage.contains(" was killed by " + username)) {
            this.addKill();
        } else if (textMessage.contains(username + " was killed ")) {
            this.resetKillstreak();
        }
        // Throw Out
        else if (textMessage.contains(" was punched into the void by " + username)) {
            this.addKill();
        } else if (textMessage.contains(username + " was punched into the void by ") || textMessage.contains(username + " became one with the void!")) {
            this.resetKillstreak();
        }
    }

    public String getRenderingString() {
        if (this.showGreatest) {
            return GREATEST_KILLSTREAK_DISPLAY_TEXT + this.greatestKillstreak;
        } else {
            return CURRENT_KILLSTREAK_DISPLAY_TEXT + this.currentKillstreak;
        }
    }

    private void addKill() {
        this.currentKillstreak++;
        if (this.currentKillstreak > this.greatestKillstreak) {
            this.greatestKillstreak = this.currentKillstreak;
        }
    }

    private void resetKillstreak() {
        this.currentKillstreak = 0;
    }

    @Override
    public void onTickUpdate() {
    }

    @Override
    public void onGameStart() {
    }


    @Override
    public boolean showElement() {
        return doesMatchForGame() && enabled;
    }

    @Override
    public String content() {
        return getRenderingString();
    }

    @Override
    public boolean ignoreEmptyCheck() {
        return false;
    }

    @Override
    public String getAfterstats() {
        return YELLOW + "Your greatest Killstreak: " + GREEN + greatestKillstreak + YELLOW + " Kills";
    }
}
