package me.earth.earthhack.impl.hud.text.coordinates;

import me.earth.earthhack.api.hud.HudCategory;
import me.earth.earthhack.api.hud.HudElement;
import me.earth.earthhack.api.setting.Setting;
import me.earth.earthhack.api.setting.settings.BooleanSetting;
import me.earth.earthhack.api.setting.settings.NumberSetting;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.util.math.MathUtil;
import me.earth.earthhack.impl.util.render.hud.HudRenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Random;

public class Coordinates extends HudElement {

    private final Setting<Boolean> dimension =
            register(new BooleanSetting("Opposite-Dimension", true));
    private final Setting<Boolean> random =
            register(new BooleanSetting("SmartRandom", false));
    private final Setting<Integer> randomRange =
            register(new NumberSetting<>("RandomRange", 5000, 1000, 50_000));

    private final Random rng = new Random();
    private String coords = "";
    private Vec3i startingPos = null;
    private Vec3i realPos = null;

    protected void onRender(DrawContext context) {
        Vec3i pos = smartCoords();
        long x = pos.getX();
        long y = pos.getY();
        long z = pos.getZ();

        String overworld = String.format(Formatting.FORMATTING_CODE_PREFIX + "f%s" + Formatting.FORMATTING_CODE_PREFIX + "8, " + Formatting.FORMATTING_CODE_PREFIX + "f%s" + Formatting.FORMATTING_CODE_PREFIX + "8, " + Formatting.FORMATTING_CODE_PREFIX + "f%s", x, y, z);

        if (dimension.getValue())
            coords = getDimension() == -1
                    ? String.format(Formatting.FORMATTING_CODE_PREFIX + "7%s " + Formatting.FORMATTING_CODE_PREFIX + "f" + "%s" + Formatting.FORMATTING_CODE_PREFIX + "8, " + Formatting.FORMATTING_CODE_PREFIX + "7%s" + Formatting.FORMATTING_CODE_PREFIX + "8, " + Formatting.FORMATTING_CODE_PREFIX + "7%s " + Formatting.FORMATTING_CODE_PREFIX + "f" + "%s", x, surroundWithBrackets(String.valueOf(x * 8)), y, z, surroundWithBrackets(String.valueOf(z * 8)))
                    : (getDimension() == 0 ? String.format(Formatting.FORMATTING_CODE_PREFIX + "f%s " + Formatting.FORMATTING_CODE_PREFIX + "7" + "%s" + Formatting.FORMATTING_CODE_PREFIX + "8, " + Formatting.FORMATTING_CODE_PREFIX + "f%s" + Formatting.FORMATTING_CODE_PREFIX + "8, " + Formatting.FORMATTING_CODE_PREFIX + "f%s " + Formatting.FORMATTING_CODE_PREFIX + "7" + "%s", x, surroundWithBrackets(String.valueOf(x / 8)), y, z, surroundWithBrackets(String.valueOf(z / 8))) : overworld);
        else
            coords = overworld;

        HudRenderUtil.renderText(context, coords, getX(), getY());
    }

    private int getDimension() {
        return switch (mc.world.getRegistryKey().getValue().getPath()) {
            case "the_nether" -> -1;
            case "the_end" -> 1;
            default -> 0;
        };
    }

    private Vec3i smartCoords() {
        if (MathUtil.distance2D(new Vec3d(mc.player.getX(), 0, mc.player.getZ()), new Vec3d(0,0,0)) < randomRange.getValue()) {
            return new Vec3i((int) Math.round(mc.player.getX()), (int) Math.round(mc.player.getY()), (int) Math.round(mc.player.getZ()));
        }
        if (random.getValue()) {
            if (startingPos == null) {
                realPos = new Vec3i((int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ());
                return startingPos = getRandomVec3i();
            } else if (MathUtil.distance2D(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ()), new Vec3d(realPos.getX(), realPos.getY(), realPos.getZ())) < 1000) {
                return new Vec3i((int) (startingPos.getX() - (realPos.getX() - Math.round(mc.player.getX()))), (int) Math.round(mc.player.getY()), (int) (startingPos.getZ() + (realPos.getZ() - Math.round(mc.player.getZ()))));
            } else {
                realPos = new Vec3i((int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ());
                startingPos = getRandomVec3i();
                return startingPos;
            }
        } else {
            return new Vec3i((int) Math.round(mc.player.getX()), (int) Math.round(mc.player.getY()), (int) Math.round(mc.player.getZ()));
        }
    }

    private Vec3i getRandomVec3i() {
        return new Vec3i(rng.nextInt(), (int) mc.player.getY(), rng.nextInt());
    }

    public Coordinates() {
        super("Coordinates", "Displays your coordinates.", HudCategory.Text, 110, 150);
    }

    @Override
    public float getWidth() {
        return Managers.TEXT.getStringWidth(coords.trim());
    }

    @Override
    public float getHeight() {
        return Managers.TEXT.getStringHeight();
    }
}
