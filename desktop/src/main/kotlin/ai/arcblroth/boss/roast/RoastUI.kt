package ai.arcblroth.boss.roast

import ai.arcblroth.boss.backend.ui.Area
import ai.arcblroth.boss.backend.ui.Bounds
import ai.arcblroth.boss.backend.ui.Contents
import ai.arcblroth.boss.backend.ui.Label
import ai.arcblroth.boss.backend.ui.UI
import ai.arcblroth.boss.roast.lib.ForeignOption_ForeignTextStyle
import ai.arcblroth.boss.roast.lib.ForeignOption_bool
import ai.arcblroth.boss.roast.lib.ForeignOption_u32
import ai.arcblroth.boss.roast.lib.ForeignRoastResult_Nothing
import ai.arcblroth.boss.roast.lib.Roast.None_ForeignTextStyle
import ai.arcblroth.boss.roast.lib.Roast.None_bool
import ai.arcblroth.boss.roast.lib.Roast.None_u32
import ai.arcblroth.boss.roast.lib.Roast.Some_ForeignTextStyle
import ai.arcblroth.boss.roast.lib.Roast.Some_bool
import ai.arcblroth.boss.roast.lib.Roast.Some_u32
import ai.arcblroth.boss.roast.lib.Roast.roast_area_horizontal
import ai.arcblroth.boss.roast.lib.Roast.roast_area_horizontal_right
import ai.arcblroth.boss.roast.lib.Roast.roast_area_horizontal_wrapped
import ai.arcblroth.boss.roast.lib.Roast.roast_area_label
import ai.arcblroth.boss.roast.lib.Roast.roast_area_vertical
import ai.arcblroth.boss.roast.lib.Roast.roast_area_vertical_centered
import ai.arcblroth.boss.roast.lib.Roast.roast_area_vertical_centered_justified
import ai.arcblroth.boss.roast.lib.Roast.roast_ui_area
import ai.arcblroth.boss.roast.lib.Roast.roast_ui_center_panel
import ai.arcblroth.boss.roast.lib.Roast.roast_ui_window
import jdk.incubator.foreign.Addressable
import jdk.incubator.foreign.MemoryAddress
import jdk.incubator.foreign.ResourceScope
import ai.arcblroth.boss.roast.lib.Bounds as ForeignBounds
import ai.arcblroth.boss.roast.lib.Contents as ForeignContents

private fun Contents.wrapUpcall(scope: ResourceScope) = ForeignContents.allocate(
    wrapUpcall<MemoryAddress>(
        scope,
        ForeignRoastResult_Nothing::allocate,
        ForeignRoastResult_Nothing::`tag$set`,
        ForeignRoastResult_Nothing::`err$slice`,
    ) {
        RoastArea(it).this()
    }
)

class RoastUI internal constructor(private val pointer: Long) : UI {
    override fun centerPanel(addContents: Contents) = ResourceScope.newConfinedScope().use { scope ->
        roast_ui_center_panel(scope, this.pointer, addContents.wrapUpcall(scope)).unwrapNothing()
    }

    override fun area(name: String, bounds: Bounds, addContents: Contents) =
        ResourceScope.newConfinedScope().use { scope ->
            val nameC = toRustString(scope, name)
            val boundsC = ForeignBounds.allocate(scope).apply {
                ForeignBounds.`x$set`(this, bounds.x)
                ForeignBounds.`y$set`(this, bounds.y)
                ForeignBounds.`w$set`(this, bounds.w)
                ForeignBounds.`h$set`(this, bounds.h)
            }
            roast_ui_area(
                scope,
                this.pointer,
                nameC,
                nameC.byteSize(),
                boundsC,
                addContents.wrapUpcall(scope)
            ).unwrapNothing()
        }

    override fun window(name: String, resizable: Boolean, addContents: Contents) =
        ResourceScope.newConfinedScope().use { scope ->
            val nameC = toRustString(scope, name)
            roast_ui_window(
                scope,
                this.pointer,
                nameC,
                nameC.byteSize(),
                resizable.toCBool(),
                addContents.wrapUpcall(scope)
            ).unwrapNothing()
        }
}

class RoastArea internal constructor(private var pointer: Addressable) : Area {
    override fun horizontal(addContents: Contents) = ResourceScope.newConfinedScope().use { scope ->
        roast_area_horizontal(scope, this.pointer, addContents.wrapUpcall(scope)).unwrapNothing()
    }

    override fun horizontalRight(addContents: Contents) = ResourceScope.newConfinedScope().use { scope ->
        roast_area_horizontal_right(scope, this.pointer, addContents.wrapUpcall(scope)).unwrapNothing()
    }

    override fun horizontalWrapped(addContents: Contents) = ResourceScope.newConfinedScope().use { scope ->
        roast_area_horizontal_wrapped(scope, this.pointer, addContents.wrapUpcall(scope)).unwrapNothing()
    }

    override fun vertical(addContents: Contents) = ResourceScope.newConfinedScope().use { scope ->
        roast_area_vertical(scope, this.pointer, addContents.wrapUpcall(scope)).unwrapNothing()
    }

    override fun verticalCentered(addContents: Contents) = ResourceScope.newConfinedScope().use { scope ->
        roast_area_vertical_centered(scope, this.pointer, addContents.wrapUpcall(scope)).unwrapNothing()
    }

    override fun verticalCenteredJustified(addContents: Contents) = ResourceScope.newConfinedScope().use { scope ->
        roast_area_vertical_centered_justified(scope, this.pointer, addContents.wrapUpcall(scope)).unwrapNothing()
    }

    override fun label(label: Label) = ResourceScope.newConfinedScope().use { scope ->
        val textC = toRustString(scope, label.text)
        val wrapC = if (label.wrap != null) {
            ForeignOption_bool.allocate(scope).apply {
                ForeignOption_bool.`tag$set`(this, Some_bool())
                ForeignOption_bool.`some$set`(this, label.wrap!!.toCBool())
            }
        } else {
            ForeignOption_bool.allocate(scope).apply {
                ForeignOption_bool.`tag$set`(this, None_bool())
            }
        }
        val textStyleC = if (label.textStyle != null) {
            ForeignOption_ForeignTextStyle.allocate(scope).apply {
                ForeignOption_ForeignTextStyle.`tag$set`(this, Some_ForeignTextStyle())
                ForeignOption_ForeignTextStyle.`some$set`(this, label.textStyle!!.ordinal)
            }
        } else {
            ForeignOption_ForeignTextStyle.allocate(scope).apply {
                ForeignOption_ForeignTextStyle.`tag$set`(this, None_ForeignTextStyle())
            }
        }
        val textColorC = if (label.textColor != null) {
            ForeignOption_u32.allocate(scope).apply {
                ForeignOption_u32.`tag$set`(this, Some_u32())
                ForeignOption_u32.`some$set`(this, label.textColor!!.rgba)
            }
        } else {
            ForeignOption_u32.allocate(scope).apply {
                ForeignOption_u32.`tag$set`(this, None_u32())
            }
        }
        roast_area_label(
            scope,
            this.pointer,
            textC.address(),
            textC.byteSize(),
            wrapC,
            textStyleC,
            label.backgroundColor.rgba,
            textColorC,
            label.code.toCBool(),
            label.strong.toCBool(),
            label.weak.toCBool(),
            label.strikethrough.toCBool(),
            label.underline.toCBool(),
            label.italics.toCBool(),
            label.raised.toCBool(),
        ).unwrapNothing()
    }
}
