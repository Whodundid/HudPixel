package net.unaussprechlich.project.connect.gui

import com.mojang.realmsclient.gui.ChatFormatting
import eladkay.hudpixel.ChatDetector
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.unaussprechlich.hypixel.helper.HypixelRank
import net.unaussprechlich.managedgui.lib.ConstantsMG
import net.unaussprechlich.managedgui.lib.GuiManagerMG
import net.unaussprechlich.managedgui.lib.databases.player.PlayerDatabaseMG
import net.unaussprechlich.managedgui.lib.databases.player.data.Rank
import net.unaussprechlich.managedgui.lib.event.EnumDefaultEvents
import net.unaussprechlich.managedgui.lib.event.events.KeyPressedCodeEvent
import net.unaussprechlich.managedgui.lib.event.events.KeyPressedEvent
import net.unaussprechlich.managedgui.lib.event.util.Event
import net.unaussprechlich.managedgui.lib.gui.GUI
import net.unaussprechlich.managedgui.lib.handler.MouseHandler
import net.unaussprechlich.managedgui.lib.templates.defaults.container.DefChatMessageContainer
import net.unaussprechlich.managedgui.lib.templates.defaults.container.DefPictureContainer
import net.unaussprechlich.managedgui.lib.templates.defaults.container.DefScrollableContainer
import net.unaussprechlich.managedgui.lib.templates.defaults.container.IScrollSpacerRenderer
import net.unaussprechlich.managedgui.lib.templates.tabs.containers.TabContainer
import net.unaussprechlich.managedgui.lib.templates.tabs.containers.TabListElementContainer
import net.unaussprechlich.managedgui.lib.templates.tabs.containers.TabManager
import net.unaussprechlich.managedgui.lib.util.DisplayUtil
import net.unaussprechlich.managedgui.lib.util.RGBA
import net.unaussprechlich.managedgui.lib.util.RenderUtils
import net.unaussprechlich.project.connect.container.ChatTabContainer
import org.lwjgl.input.Keyboard

/**
 * ChatGUI Created by Alexander on 24.02.2017.
 * Description:
 */
object ChatGUI : GUI() {

    internal val tabManager = TabManager()

    private val WIDTH = 500
    private val HEIGHT = 200

    private var visible = false

    var privateChatCons : MutableMap<String, ChatTabContainer> = hashMapOf()

    private val scrollSpacerRenderer = object : IScrollSpacerRenderer {
        override fun render(xStart: Int, yStart: Int, width: Int) {
            RenderUtils.renderBoxWithColorBlend_s1_d0(xStart + 25, yStart, width - 42, 1, RGBA.P1B1_596068.get())
        }
        override val spacerHeight: Int
            get() = 1
    }

    private val scrollALL = DefScrollableContainer(ConstantsMG.DEF_BACKGROUND_RGBA, WIDTH, HEIGHT - 17, scrollSpacerRenderer).apply {
        minWidth = 400
        minHeight= 200
    }
    private val partyCon = DefScrollableContainer(ConstantsMG.DEF_BACKGROUND_RGBA, WIDTH, HEIGHT - 17, scrollSpacerRenderer).apply {
        minWidth = 400
        minHeight= 200
    }
    private val guildCon = DefScrollableContainer(ConstantsMG.DEF_BACKGROUND_RGBA, WIDTH, HEIGHT - 17, scrollSpacerRenderer).apply {
        minWidth = 400
        minHeight= 200
    }
    private val privateCon = DefScrollableContainer(ConstantsMG.DEF_BACKGROUND_RGBA, WIDTH, HEIGHT - 17, scrollSpacerRenderer).apply {
        minWidth = 400
        minHeight= 200
    }

    init {
        tabManager.isVisible = false
        registerChild(tabManager)

        tabManager.registerTab(ChatTabContainer(TabListElementContainer("ALL", RGBA.WHITE.get(), tabManager), scrollALL, tabManager))
        tabManager.registerTab(ChatTabContainer(TabListElementContainer("PARTY", RGBA.BLUE.get(), tabManager), partyCon, tabManager))
        tabManager.registerTab(ChatTabContainer(TabListElementContainer("GUILD", RGBA.GREEN.get(), tabManager), guildCon, tabManager))
        tabManager.registerTab(ChatTabContainer(TabListElementContainer("PRIVATE", RGBA.PURPLE_DARK_MC.get(), tabManager), privateCon, tabManager))

        updatePosition()

        ChatDetector.registerEventHandler(ChatDetector.PrivateMessage) {
            val name = it.data["name"].toString()
            if(!privateChatCons.containsKey(name))
                openPrivateChat(name)
            addChatMessage((privateChatCons[name] as ChatTabContainer).container as DefScrollableContainer,
                    if(it.data["type"] == "From")
                        name
                    else
                        Minecraft.getMinecraft().thePlayer.name ,
                    it.data["message"].toString(),
                    if(it.data["type"] == "From")
                        HypixelRank.getRankByName(it.data["rank"].toString())
                    else
                        Rank("[YOU]", "" + ChatFormatting.YELLOW + "[YOU]", ChatFormatting.YELLOW)
            )
        }

        ChatDetector.registerEventHandler(ChatDetector.GuildChat) {
            addChatMessage(guildCon, it.data["name"].toString(), it.data["message"].toString(), HypixelRank.getRankByName(it.data["rank"].toString()))
        }

        ChatDetector.registerEventHandler(ChatDetector.PartyChat) {
            addChatMessage(partyCon, it.data["name"].toString(), it.data["message"].toString(), HypixelRank.getRankByName(it.data["rank"].toString()))
        }

    }

    fun openPrivateChat(user : String) {
        val chatCon = ChatTabContainer(TabListElementContainer(user, RGBA.PURPLE_DARK_MC.get(), tabManager),
                            DefScrollableContainer(ConstantsMG.DEF_BACKGROUND_RGBA, WIDTH, HEIGHT - 17, scrollSpacerRenderer).apply {
                                minWidth = 400
                                minHeight= 200
                            },
                            tabManager
                      )
        tabManager.registerTab(chatCon)
        if(!privateChatCons.containsKey(user))
            privateChatCons[user] to chatCon
    }

    fun closePrivateChatCon(user : String){
        if(!privateChatCons.containsKey(user)) return
        tabManager.unregisterTab(privateChatCons[user] as TabContainer)
        privateChatCons.remove(user)
    }


    fun addChatMessage(con: DefScrollableContainer, name: String, message: String, rank: Rank) {
        if (!con.scrollElements.isEmpty() && con.scrollElements[con.scrollElements.size - 1] is DefChatMessageContainer) {
            val conMes = con.scrollElements[con.scrollElements.size - 1] as DefChatMessageContainer
            if (conMes.playername.equals(name, ignoreCase = true)) {
                conMes.addMessage(message)
                return
            }
        }
        PlayerDatabaseMG.get(name){ player ->
            con.registerScrollElement(
                    DefChatMessageContainer(
                            player,
                            message,
                            DefPictureContainer(),
                            WIDTH
                    )
            )
        }

    }

    private fun updatePosition() {
        tabManager.xOffset = 5
        tabManager.yOffset = DisplayUtil.scaledMcHeight - HEIGHT - 17 - 30
    }


    override fun doClientTick(): Boolean {
        return true
    }

    override fun doRender(xStart: Int, yStart: Int): Boolean {

        //GL11.glColor3f(1f, 1f, 1f)
        //GuiInventory.drawEntityOnScreen(1000, 500, 100, MouseHandler.getmX(), MouseHandler.getmY(), Minecraft.getMinecraft().thePlayer);
        return true
    }

    override fun doChatMessage(e: ClientChatReceivedEvent): Boolean {
        return true
    }

    override fun doMouseMove(mX: Int, mY: Int): Boolean {
        return true
    }

    override fun doScroll(i: Int): Boolean {
        return true
    }

    override fun doClick(clickType: MouseHandler.ClickType): Boolean {
        return true
    }

    override fun <T : Event<*>> doEventBus(event: T): Boolean {
        if (event.id == EnumDefaultEvents.SCREEN_RESIZE.get()) {
            updatePosition()
        } else if (event.id == EnumDefaultEvents.KEY_PRESSED.get()){
            if((event as KeyPressedEvent).data.equals("k" , true)){
                GuiManagerMG.bindScreen()
                tabManager.isVisible = true
                Keyboard.enableRepeatEvents(true)
            }
        }else if (event.id == EnumDefaultEvents.KEY_PRESSED_CODE.get()){
            if((event as KeyPressedCodeEvent).data == Keyboard.KEY_ESCAPE){
                tabManager.isVisible = false
                Keyboard.enableRepeatEvents(false)
            }
        }
        return true
    }

    override fun doOpenGUI(e: GuiOpenEvent): Boolean {
        return true
    }

    override fun doResize(): Boolean {
        return true
    }


}
