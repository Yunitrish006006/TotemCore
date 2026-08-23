package dev.totem.core.mixin.client;

import dev.totem.core.api.v1.client.manual.TotemManualPageOverlayRegistry;
import dev.totem.core.api.v1.client.manual.TotemManualPageRenderContext;
import dev.totem.core.api.v1.manual.TotemManualAssembler;
import dev.totem.core.api.v1.manual.TotemManualSection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.input.MouseButtonEvent;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/** Gives canonical Totem manuals an unlimited adaptive two-page book with a clickable contents index. */
@Mixin(BookViewScreen.class)
abstract class BookViewScreenMixin extends Screen {
    @Unique private static final Identifier totem$LEFT_PAGE_BACKGROUND =
            Identifier.fromNamespaceAndPath("totem-core", "textures/gui/manual_left_page.png");

    @Unique private static final int totem$COMPACT_PAGE_WIDTH = 192;
    @Unique private static final int totem$COMPACT_PAGE_HEIGHT = 192;
    @Unique private static final int totem$COMPACT_PAGE_STRIDE = 146;
    @Unique private static final int totem$COMPACT_TEXT_WIDTH = 114;
    @Unique private static final int totem$COMPACT_TEXT_HEIGHT = 128;

    @Unique private static final int totem$ROOMY_PAGE_WIDTH = 224;
    @Unique private static final int totem$ROOMY_PAGE_HEIGHT = 224;
    @Unique private static final int totem$ROOMY_PAGE_STRIDE = 170;
    @Unique private static final int totem$ROOMY_TEXT_WIDTH = 146;
    @Unique private static final int totem$ROOMY_TEXT_HEIGHT = 160;

    @Unique private static final int totem$PAGE_SOURCE_WIDTH = 192;
    @Unique private static final int totem$PAGE_SOURCE_HEIGHT = 192;
    @Unique private static final int totem$MIN_SCREEN_MARGIN = 12;
    @Unique private static final int totem$TEXT_LEFT = 36;
    @Unique private static final int totem$TEXT_TOP = 30;
    @Unique private static final int totem$CONTENTS_TOP = 50;
    @Unique private static final int totem$PAGE_NUMBER_RIGHT_MARGIN = 44;
    @Unique private static final int totem$MENU_BUTTON_WIDTH = 200;
    @Unique private static final int totem$MENU_BUTTON_HEIGHT = 20;
    @Unique private static final int totem$MENU_BUTTON_GAP = 6;
    @Unique private static final int totem$CONTROL_STRIP_HEIGHT =
            totem$MENU_BUTTON_GAP + totem$MENU_BUTTON_HEIGHT;
    @Unique private static final Style totem$PAGE_TEXT_STYLE =
            Style.EMPTY.withoutShadow().withColor(0x000000);

    @Shadow private BookViewScreen.BookAccess bookAccess;
    @Shadow private int currentPage;
    @Shadow private PageButton forwardButton;
    @Shadow private PageButton backButton;

    @Unique private boolean totem$virtualPagesResolved;
    @Unique private List<TotemManualSection> totem$manualSections = List.of();
    @Unique private List<Component> totem$virtualPages = List.of();

    protected BookViewScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "createMenuControls", at = @At("HEAD"), cancellable = true)
    private void totem$createAdaptiveMenuControls(CallbackInfo callback) {
        if (!totem$isManual()) {
            return;
        }
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.done"),
                        button -> onClose())
                .bounds(totem$doneButtonX(), totem$doneButtonY(),
                        totem$MENU_BUTTON_WIDTH, totem$MENU_BUTTON_HEIGHT)
                .build());
        callback.cancel();
    }

    @Inject(method = "createPageControlButtons", at = @At("TAIL"))
    private void totem$positionSpreadButtons(CallbackInfo callback) {
        if (!totem$isManual()) {
            return;
        }
        totem$ensureVirtualPages();
        int spreadLeft = totem$spreadLeft();
        int pageTop = totem$pageTop();
        int pageHeight = totem$pageHeight();
        int navigationY = pageTop + pageHeight - 35;

        backButton.setX(spreadLeft + 43);
        backButton.setY(navigationY);
        forwardButton.setX(spreadLeft + totem$pageStride() + totem$pageWidth() - 76);
        forwardButton.setY(navigationY);

        int contentsX = spreadLeft >= 62 ? spreadLeft - 58 : spreadLeft + 34;
        addRenderableWidget(Button.builder(
                        Component.translatable("button.totem.manual.contents"),
                        button -> {
                            currentPage = 0;
                            totem$updateButtonVisibility();
                        })
                .bounds(contentsX, pageTop + 8, 54, 20)
                .build());
        totem$updateButtonVisibility();
    }

    @Inject(method = "setPage", at = @At("HEAD"), cancellable = true)
    private void totem$setSpreadPage(int requestedPage, CallbackInfoReturnable<Boolean> callback) {
        if (!totem$isManual()) {
            return;
        }
        totem$ensureVirtualPages();
        int lastPage = Math.max(0, totem$pageCount() - 1);
        int target = Mth.clamp(requestedPage, 0, lastPage) & ~1;
        if (target == currentPage) {
            callback.setReturnValue(false);
            return;
        }
        currentPage = target;
        totem$updateButtonVisibility();
        callback.setReturnValue(true);
    }

    @Inject(method = "pageBack", at = @At("HEAD"), cancellable = true)
    private void totem$pageSpreadBack(CallbackInfo callback) {
        if (!totem$isManual()) {
            return;
        }
        totem$ensureVirtualPages();
        currentPage = Math.max(0, currentPage - 2);
        totem$updateButtonVisibility();
        callback.cancel();
    }

    @Inject(method = "pageForward", at = @At("HEAD"), cancellable = true)
    private void totem$pageSpreadForward(CallbackInfo callback) {
        if (!totem$isManual()) {
            return;
        }
        totem$ensureVirtualPages();
        int lastSpread = Math.max(0, totem$pageCount() - 1) & ~1;
        currentPage = Math.min(lastSpread, currentPage + 2);
        totem$updateButtonVisibility();
        callback.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void totem$clickContentsEntry(
            MouseButtonEvent event,
            boolean doubleClick,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (!totem$isManual() || event.button() != 0) {
            return;
        }
        totem$ensureVirtualPages();
        int spreadLeft = totem$spreadLeft();
        int sectionIndex = totem$contentsEntryAt(
                event.x(), event.y(), currentPage, spreadLeft
        );
        if (sectionIndex < 0 && currentPage + 1 < totem$pageCount()) {
            sectionIndex = totem$contentsEntryAt(
                    event.x(), event.y(), currentPage + 1, spreadLeft + totem$pageStride()
            );
        }
        if (sectionIndex < 0) {
            return;
        }
        int targetPage = TotemManualAssembler.sectionStartPage(totem$manualSections, sectionIndex);
        currentPage = targetPage & ~1;
        totem$updateButtonVisibility();
        callback.setReturnValue(true);
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
        int pageTop = totem$pageTop();
        int pageWidth = totem$pageWidth();
        int pageHeight = totem$pageHeight();
        graphics.blit(RenderPipelines.GUI_TEXTURED, totem$LEFT_PAGE_BACKGROUND,
                spreadLeft, pageTop, 0.0F, 0.0F,
                pageWidth, pageHeight,
                totem$PAGE_SOURCE_WIDTH, totem$PAGE_SOURCE_HEIGHT,
                256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BookViewScreen.BOOK_LOCATION,
                spreadLeft + totem$pageStride(), pageTop, 0.0F, 0.0F,
                pageWidth, pageHeight,
                totem$PAGE_SOURCE_WIDTH, totem$PAGE_SOURCE_HEIGHT,
                256, 256);
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
        totem$ensureVirtualPages();
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int spreadLeft = totem$spreadLeft();
        totem$renderPage(graphics, currentPage, spreadLeft, mouseX, mouseY);
        int rightPage = currentPage + 1;
        if (rightPage < totem$pageCount()) {
            totem$renderPage(graphics, rightPage, spreadLeft + totem$pageStride(), mouseX, mouseY);
        }
        callback.cancel();
    }

    @Unique
    private void totem$renderPage(
            GuiGraphicsExtractor graphics,
            int pageIndex,
            int pageLeft,
            int mouseX,
            int mouseY
    ) {
        int pageTop = totem$pageTop();
        Component pageNumber = Component.translatable(
                "book.pageIndicator",
                pageIndex + 1,
                Math.max(1, totem$pageCount())
        ).withStyle(totem$PAGE_TEXT_STYLE);
        graphics.text(font, pageNumber,
                pageLeft + totem$pageWidth() - totem$PAGE_NUMBER_RIGHT_MARGIN - font.width(pageNumber),
                pageTop + 16, 0xFF000000, false);

        if (totem$isContentsPage(pageIndex)) {
            totem$renderContentsPage(graphics, pageIndex, pageLeft, mouseX, mouseY);
            return;
        }

        Component page = ComponentUtils.mergeStyles(totem$page(pageIndex), totem$PAGE_TEXT_STYLE);
        List<FormattedCharSequence> lines = font.split(page, totem$textWidth());
        int lineCount = Math.min(totem$textHeight() / font.lineHeight, lines.size());
        for (int line = 0; line < lineCount; line++) {
            graphics.text(font, lines.get(line),
                    pageLeft + totem$TEXT_LEFT, pageTop + totem$TEXT_TOP + line * font.lineHeight,
                    0xFF000000, false);
        }

        TotemManualPageOverlayRegistry.render(new TotemManualPageRenderContext(
                graphics,
                font,
                totem$pageKey(pageIndex),
                pageLeft,
                pageTop,
                mouseX,
                mouseY
        ));
    }

    @Unique
    private void totem$renderContentsPage(
            GuiGraphicsExtractor graphics,
            int pageIndex,
            int pageLeft,
            int mouseX,
            int mouseY
    ) {
        int pageTop = totem$pageTop();
        int contentsIndex = pageIndex - 1;
        int contentsPages = TotemManualAssembler.contentsPageCount(totem$manualSections.size());
        int centerX = pageLeft + totem$pageWidth() / 2 - 3;
        graphics.centeredText(font,
                Component.translatable(TotemManualAssembler.CONTENTS_PAGE_KEY),
                centerX, pageTop + 31, 0xFF000000);
        if (contentsPages > 1) {
            graphics.centeredText(font,
                    Component.literal((contentsIndex + 1) + "/" + contentsPages),
                    centerX, pageTop + 40, 0xFF6F5637);
        }

        int firstSection = contentsIndex * TotemManualAssembler.CONTENTS_ENTRIES_PER_PAGE;
        int rowHeight = font.lineHeight + 2;
        for (int row = 0; row < TotemManualAssembler.CONTENTS_ENTRIES_PER_PAGE; row++) {
            int sectionIndex = firstSection + row;
            if (sectionIndex >= totem$manualSections.size()) {
                break;
            }
            int y = pageTop + totem$CONTENTS_TOP + row * rowHeight;
            boolean hovered = mouseX >= pageLeft + 34 && mouseX < pageLeft + 38 + totem$textWidth()
                    && mouseY >= y && mouseY < y + rowHeight;
            TotemManualSection section = totem$manualSections.get(sectionIndex);
            int targetPage = TotemManualAssembler.sectionStartPage(totem$manualSections, sectionIndex);
            Component label = Component.literal("• ")
                    .append(Component.translatable(section.titleKey()))
                    .append(Component.literal("  " + (targetPage + 1)));
            List<FormattedCharSequence> fitted = font.split(label, totem$textWidth());
            if (!fitted.isEmpty()) {
                graphics.text(font, fitted.getFirst(), pageLeft + totem$TEXT_LEFT, y,
                        hovered ? 0xFF285F91 : 0xFF000000, false);
            }
        }
    }

    @Unique
    private int totem$contentsEntryAt(
            double mouseX,
            double mouseY,
            int pageIndex,
            int pageLeft
    ) {
        if (!totem$isContentsPage(pageIndex)) {
            return -1;
        }
        int rowHeight = font.lineHeight + 2;
        double top = totem$pageTop() + totem$CONTENTS_TOP;
        if (mouseX < pageLeft + 34 || mouseX >= pageLeft + 38 + totem$textWidth()
                || mouseY < top || mouseY >= top + rowHeight * TotemManualAssembler.CONTENTS_ENTRIES_PER_PAGE) {
            return -1;
        }
        int row = (int) ((mouseY - top) / rowHeight);
        int contentsIndex = pageIndex - 1;
        int sectionIndex = contentsIndex * TotemManualAssembler.CONTENTS_ENTRIES_PER_PAGE + row;
        return sectionIndex < totem$manualSections.size() ? sectionIndex : -1;
    }

    @Unique
    private boolean totem$isContentsPage(int pageIndex) {
        if (totem$manualSections.isEmpty()) {
            return pageIndex == 1;
        }
        int contentsPages = TotemManualAssembler.contentsPageCount(totem$manualSections.size());
        return pageIndex >= 1 && pageIndex <= contentsPages;
    }

    @Unique
    private void totem$ensureVirtualPages() {
        if (totem$virtualPagesResolved) {
            return;
        }
        totem$virtualPagesResolved = true;
        if (!totem$isManual()) {
            return;
        }

        if (bookAccess.getPageCount() >= 2) {
            Component indexPage = bookAccess.getPage(1);
            String raw = indexPage.getString();
            if (raw.startsWith(TotemManualAssembler.MANUAL_INDEX_PREFIX)) {
                totem$manualSections = TotemManualAssembler.sectionsFromIndexPage(indexPage);
                totem$virtualPages = TotemManualAssembler.virtualPages(totem$manualSections);
                return;
            }
        }

        // Schema 1/2 fallback: render the physical pages until login refresh migrates the item to schema 3.
        List<Component> legacyPages = new ArrayList<>(bookAccess.getPageCount());
        for (int page = 0; page < bookAccess.getPageCount(); page++) {
            legacyPages.add(bookAccess.getPage(page));
        }
        totem$virtualPages = List.copyOf(legacyPages);
    }

    @Unique
    private void totem$updateButtonVisibility() {
        int pageCount = totem$pageCount();
        backButton.visible = currentPage > 0;
        forwardButton.visible = currentPage + 2 < pageCount;
    }

    @Unique
    private int totem$pageCount() {
        return totem$virtualPagesResolved && !totem$virtualPages.isEmpty()
                ? totem$virtualPages.size()
                : bookAccess.getPageCount();
    }

    @Unique
    private Component totem$page(int pageIndex) {
        if (totem$virtualPagesResolved && pageIndex >= 0 && pageIndex < totem$virtualPages.size()) {
            return totem$virtualPages.get(pageIndex);
        }
        return bookAccess.getPage(pageIndex);
    }

    @Unique
    private boolean totem$isManual() {
        return bookAccess.getPageCount() > 0
                && TotemManualAssembler.COVER_PAGE_KEY.equals(totem$physicalPageKey(0));
    }

    @Unique
    private String totem$pageKey(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= totem$pageCount()) {
            return null;
        }
        Component page = totem$page(pageIndex);
        if (page.getContents() instanceof TranslatableContents translated) {
            return translated.getKey();
        }
        return null;
    }

    @Unique
    private String totem$physicalPageKey(int pageIndex) {
        if (pageIndex < 0 || pageIndex >= bookAccess.getPageCount()) {
            return null;
        }
        if (bookAccess.getPage(pageIndex).getContents() instanceof TranslatableContents translated) {
            return translated.getKey();
        }
        return null;
    }

    @Unique
    private float totem$layoutExpansion() {
        int compactSpread = totem$COMPACT_PAGE_WIDTH + totem$COMPACT_PAGE_STRIDE;
        int roomySpread = totem$ROOMY_PAGE_WIDTH + totem$ROOMY_PAGE_STRIDE;
        float widthRoom = (width - totem$MIN_SCREEN_MARGIN - compactSpread)
                / (float) (roomySpread - compactSpread);
        float heightRoom = (height - totem$MIN_SCREEN_MARGIN - totem$CONTROL_STRIP_HEIGHT
                - totem$COMPACT_PAGE_HEIGHT)
                / (float) (totem$ROOMY_PAGE_HEIGHT - totem$COMPACT_PAGE_HEIGHT);
        return Mth.clamp(Math.min(widthRoom, heightRoom), 0.0F, 1.0F);
    }

    @Unique
    private int totem$layoutValue(int compact, int roomy) {
        return compact + Math.round((roomy - compact) * totem$layoutExpansion());
    }

    @Unique
    private int totem$pageWidth() {
        return totem$layoutValue(totem$COMPACT_PAGE_WIDTH, totem$ROOMY_PAGE_WIDTH);
    }

    @Unique
    private int totem$pageHeight() {
        return totem$layoutValue(totem$COMPACT_PAGE_HEIGHT, totem$ROOMY_PAGE_HEIGHT);
    }

    @Unique
    private int totem$pageStride() {
        return totem$layoutValue(totem$COMPACT_PAGE_STRIDE, totem$ROOMY_PAGE_STRIDE);
    }

    @Unique
    private int totem$textWidth() {
        return totem$layoutValue(totem$COMPACT_TEXT_WIDTH, totem$ROOMY_TEXT_WIDTH);
    }

    @Unique
    private int totem$textHeight() {
        return totem$layoutValue(totem$COMPACT_TEXT_HEIGHT, totem$ROOMY_TEXT_HEIGHT);
    }

    @Unique
    private int totem$pageTop() {
        int occupiedHeight = totem$pageHeight() + totem$CONTROL_STRIP_HEIGHT;
        return Math.max(2, (height - occupiedHeight) / 2);
    }

    @Unique
    private int totem$doneButtonX() {
        return (width - totem$MENU_BUTTON_WIDTH) / 2;
    }

    @Unique
    private int totem$doneButtonY() {
        return totem$pageTop() + totem$pageHeight() + totem$MENU_BUTTON_GAP;
    }

    @Unique
    private int totem$spreadLeft() {
        int spreadWidth = totem$pageWidth() + totem$pageStride();
        return (width - spreadWidth) / 2;
    }
}
