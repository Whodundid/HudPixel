package com.palechip.hudpixelmod.games;

import com.palechip.hudpixelmod.HudPixelConfig;
import com.palechip.hudpixelmod.components.CoinCounterComponent;

import net.hypixel.api.util.GameType;

public class UHC extends Game {

    protected UHC() {
        super("", "UHC Champions", START_MESSAGE_DEFAULT, END_MESSAGE_DEFAULT, GameType.UHC);
        if(HudPixelConfig.uhcCoinDisplay) {
            this.components.add(new CoinCounterComponent());
        }
    }

}