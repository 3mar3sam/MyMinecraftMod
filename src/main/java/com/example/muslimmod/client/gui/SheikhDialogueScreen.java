package com.example.muslimmod.client.gui;

import com.example.muslimmod.network.SheikhDialoguePayload;
import com.example.muslimmod.network.SheikhDialogueResponsePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class SheikhDialogueScreen extends Screen {
    private final int entityId;
    private final String sheikhName;
    private final int dialogueType;

    public SheikhDialogueScreen(int entityId, String sheikhName, int dialogueType) {
        super(Component.literal(sheikhName));
        this.entityId = entityId;
        this.sheikhName = sheikhName;
        this.dialogueType = dialogueType;
    }

    public static void open(int entityId, String sheikhName) {
        open(entityId, sheikhName, SheikhDialoguePayload.TYPE_TRADE_OUTSIDE);
    }

    public static void open(int entityId, String sheikhName, int dialogueType) {
        Minecraft.getInstance().setScreen(new SheikhDialogueScreen(entityId, sheikhName, dialogueType));
    }

    private Component getTitleText(boolean isQuest) {
        return isQuest
                ? Component.literal("§6§lالشيخ - عهد ذو الفقار")
                : Component.literal("§6§lالشيخ - آداب المسجد والتجارة");
    }

    private Component getSubtitleText(boolean isQuest) {
        return isQuest
                ? Component.literal("§e«دفع ضرر دودة الرمال العظيمة عن العباد»")
                : Component.literal("§c«إن المساجد لم تُبنَ لهذا، وإنما بُنيت لذكر الله والصلاة»");
    }

    private Component getBodyText(boolean isQuest) {
        return isQuest
                ? Component.literal("§fيا هذا، إن في أعماق الصحراء دودة عظيمة قد أضرت بالناس وأهلكت الحرث.\n\n§fعندي سيف متوارث عن أجدادي (§6سيف ذو الفقار§f) أعطيك إياه عارية بشرط:\n§cألا تستعمله إلا في مواجهة تلك الدودة§f، فإن هزمتها وأتيتني بسنها صار لك للأبد.\n\n§eفهل تقبل العهد والميثاق؟")
                : Component.literal("§fالمسجد بيت للعبادة والصلاة، ولا يجوز فيه البيع والشراء حفظاً لحرمته وسكينة المصلين.\n\n§eهل تود مرافقتي إلى خارج المسجد لإتمام التجارة؟");
    }

    private int calculateDialogHeight(int dialogWidth, boolean isQuest) {
        int textWidth = dialogWidth - 36;
        int textHeight = this.font.wordWrapHeight(getBodyText(isQuest), textWidth);
        int neededHeight = 52 + textHeight + 16 + 22 + 14;
        return Math.min(this.height - 20, Math.max(160, neededHeight));
    }

    @Override
    protected void init() {
        super.init();
        boolean isQuest = this.dialogueType == SheikhDialoguePayload.TYPE_QUEST_VOW;

        int dialogWidth = Math.min(390, this.width - 24);
        int dialogHeight = calculateDialogHeight(dialogWidth, isQuest);
        int x = (this.width - dialogWidth) / 2;
        int y = (this.height - dialogHeight) / 2;

        int buttonWidth = (dialogWidth - 40) / 2;
        int buttonHeight = 22;
        int buttonY = y + dialogHeight - 34;

        Component yesText = isQuest
                ? Component.literal("§6✔ قبلت العهد")
                : Component.literal("§a✔ نعم، نخرج للتجارة");
        Component noText = isQuest
                ? Component.literal("§7✖ أرفض العهد")
                : Component.literal("§c✖ لا، سأبقى هنا");

        // Accept / Yes Button
        this.addRenderableWidget(Button.builder(
                yesText,
                b -> {
                    PacketDistributor.sendToServer(new SheikhDialogueResponsePayload(this.entityId, true, this.dialogueType));
                    this.onClose();
                }
        ).bounds(x + 15, buttonY, buttonWidth, buttonHeight).build());

        // Decline / No Button
        this.addRenderableWidget(Button.builder(
                noText,
                b -> {
                    PacketDistributor.sendToServer(new SheikhDialogueResponsePayload(this.entityId, false, this.dialogueType));
                    this.onClose();
                }
        ).bounds(x + dialogWidth - buttonWidth - 15, buttonY, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        boolean isQuest = this.dialogueType == SheikhDialoguePayload.TYPE_QUEST_VOW;

        int dialogWidth = Math.min(390, this.width - 24);
        int dialogHeight = calculateDialogHeight(dialogWidth, isQuest);
        int x = (this.width - dialogWidth) / 2;
        int y = (this.height - dialogHeight) / 2;
        int textWidth = dialogWidth - 36;

        // 1. Soft ambient outer shadow
        guiGraphics.fill(x - 3, y - 3, x + dialogWidth + 3, y + dialogHeight + 3, 0x88000000);

        // 2. Main background (dark obsidian luxury panel)
        guiGraphics.fill(x, y, x + dialogWidth, y + dialogHeight, 0xF8111319);

        // 3. Elegant Gold Double Border
        guiGraphics.renderOutline(x, y, dialogWidth, dialogHeight, 0xFFD4AF37);
        guiGraphics.renderOutline(x + 2, y + 2, dialogWidth - 4, dialogHeight - 4, 0x44D4AF37);

        // 4. Header title banner background & decorative divider
        guiGraphics.fill(x + 3, y + 3, x + dialogWidth - 3, y + 26, 0x33D4AF37);
        guiGraphics.fill(x + 12, y + 26, x + dialogWidth - 12, y + 27, 0x88D4AF37);

        // 5. Title text
        guiGraphics.drawCenteredString(this.font, getTitleText(isQuest), this.width / 2, y + 10, 0xFFFFE082);

        // 6. Subtitle / Hadith quote
        guiGraphics.drawCenteredString(this.font, getSubtitleText(isQuest), this.width / 2, y + 32, 0xFFFFCC80);

        // 7. Body text with word wrap (safely contained in the box, never overlapping buttons)
        guiGraphics.drawWordWrap(this.font, getBodyText(isQuest), x + 18, y + 50, textWidth, 0xFFF0F0F0);

        // 8. Render widgets (buttons) on top of the dialog
        for (net.minecraft.client.gui.components.Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
