package dev.totem.core.mixin.client;

import dev.totem.core.api.v1.client.manual.TotemManualPageOverlayRegistry;
import dev.totem.core.api.v1.client.manual.TotemManualPageRenderContext;
import dev.totem.core.api.v1.manual.TotemManualAssembler;
import dev.totem.core.network.SplitTotemManualPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Gives only canonical Totem manuals a shared two-page vanilla-book presentation. */
@Mixin(BookViewScreen.class)
abstract class BookViewScreenMixin extends Screen {
    private static final Identifier LEFT_PAGE_BACKGROUND =
            Identifier.fromNamespaceAndPath("totem-core", "textures/gui/manual_left_page.png");
    private static final int PAGE_WIDTH = 192;
    private static final int PAGE_HEIGHT = 192;
    private static final int PAGE_STRIDE = 146;
    private static final int SPREAD_WIDTH = PAGE_WIDTH + PAGE_STRIDE;
    private static final int PAGE_TOP = 2;
    private static final int TEXT_WIDTH = 114;
    private static final int TEXT_HEIGHT = 128;
    private static final Style PAGE_TEXT_STYLE = Style.EMPTY.withoutShadow().withColor(0x000000);

    @Shadow
    private BookViewScreen.BookAccess bookAccess;

    @Shadow
    private int currentPage;

    @Shadow
    private PageButton forwardButton;

    @Shadow
    private PageButton backButton;

    @Shadow
    protected abstract void updateButtonVisibility();

    protected BookViewScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "createPageControlButtons", at = @At("TAIL"))
    private void totem$positionSpreadButtons(CallbackInfo callback) {
        if (!totem$isManual()) {
            return;
        }
        int spreadLeft = totem$spreadLeft();
        backButton.setX(spreadLeft + 43);
        forwardButton.setX(spreadLeft + PAGE_STRIDE + 116);
        Button splitButton = Button.builder(
                        Component.translatable("button.totem.manual.split"),
                        button -> {
                            if (ClientPlayNetworking.canSend(SplitTotemManualPayload.TYPE)) {
                                ClientPlayNetworking.send(SplitTotemManualPayload.INSTANCE);
                                onClose();
                            }
                        })
                .bounds(Math.max(4, spreadLeft - 44), PAGE_TOP + 8, 40, 20)
                .build();
        splitButton.active = ClientPlayNetworking.canSend(SplitTotemManualPayload.TYPE);
        addRenderableWidget(splitButton);
    }

    @Inject(method = "setPage", at = @At("HEAD"), cancellable = true)
    private void totem$setSpreadPage(int requestedPage, CallbackInfoReturnable<Boolean> callback) {
        if (!totem$isManual()) {
            return;
        }
        int lastPage = Math.max(0, bookAccess.getPageCount() - 1);
        int target = Mth.clamp(requestedPage, 0, lastPage) & ~1;
        if (target == currentPage) {
            callback.setReturnValue(false);
            return;
        }
        currentPage = target;
        updateButtonVisibility();
        callback.setReturnValue(true);
    }

    @Inject(method = "pageBack", at = @At("HEAD"), cancellable = true)
    private void totem$pageSpreadBack(CallbackInfo callback) {
        if (!totem$isManual()) {
            return;
        }
        currentPage = Math.max(0, currentPage - 2);
        updateButtonVisibility();
        callback.cancel();
    }

    @Inject(method = "pageForward", at = @At("HEAD"), cancellable = true)
    private void totem$pageSpreadForward(CallbackInfo callback) {
        if (!totem$isManual()) {
            return;
        }
        int lastSpread = Math.max(0, bookAccess.getPageCount() - 1) & ~1;
        currentPage = Math.min(lastSpread, currentPage + 2);
        updateButtonVisibility();
        callback.cancel();
    }

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true)
    private void totem$renderSpreadBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo callback
    ) {
        if (!totem$isManual()) {
            return;
        }
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int spreadLeft = totem$spreadLeft();
        graphics.blit(RenderPipelines.GUI_TEXTURED, LEFT_PAGE_BACKGROUND,
                spreadLeft, PAGE_TOP, 0.0F, 0.0F,
                PAGE_WIDTH, PAGE_HEIGHT, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BookViewScreen.BOOK_LOCATION,
                spreadLeft + PAGE_STRIDE, PAGE_TOP, 0.0F, 0.0F,
                PAGE_WIDTH, PAGE_HEIGHT, 256, 256);
        callback.cancel();
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
    private void totem$renderSpread(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo callback
    ) {
        if (!totem$isManual()) {
            return;
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int spreadLeft = totem$spreadLeft();
        totem$renderPage(graphics, currentPage, spreadLeft, mouseX, mouseY);
        int rightPage = currentPage + 1;
        if (rightPage < bookAccess.getPageCount()) {
            totem$renderPage(graphics, rightPage, spreadLeft + PAGE_STRIDE, mouseX, mouseY);
        }
        callback.cancel();
    }

    private void totem$renderPage(
            GuiGraphicsExtractor graphics,
            int pageIndex,
            int pageLeft,
            int mouseX,
            int mouseY
    ) {
        Component pageNumber = Component.translatable(
                "book.pageIndicator",
                pageIndex + 1,
                Math.max(1, bookAccess.getPageCount())
        ).withStyle(PAGE_TEXT_STYLE);
        graphics.text(font, pageNumber,
                pageLeft + 148 - font.width(pageNumber), PAGE_TOP + 16, 0xFF000000, false);

        Component page = ComponentUtils.mergeStyles(bookAccess.getPage(pageIndex), PAGE_TEXT_STYLE);
        List<FormattedCharSequence> lines = font.split(page, TEXT_WIDTH);
        int lineCount = Math.min(TEXT_HEIGHT / font.lineHeight, lines.size());
        for (int line = 0; line < lineCount; line++) {
            graphics.text(font, lines.get(line),
                    pageLeft + 36, PAGE_TOP + 30 + line * font.lineHeight,
                    0xFF000000, false);
        }

        TotemManualPageOverlayRegistry.render(new TotemManualPageRenderContext(
                graphics,
                font,
                totem$pageKey(pageIndex),
                pageLeft,
                PAGE_TOP,
                mouseX,
                mouseY
        ));
    }

    private boolean totem$isManual() {
        return bookAccess.getPageCount() > 0
                && TotemManualAssembler.COVER_PAGE_KEY.equals(totem$pageKey(0));
    }

    private String totem$pageKey(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= bookAccess.getPageCount()) {
            return null;
        }
        if (bookAccess.getPage(pageIndex).getContents() instanceof TranslatableContents translated) {
            return translated.getKey();
        }
        return null;
    }

    private int totem$spreadLeft() {
        return (width - SPREAD_WIDTH) / 2;
    }
}
