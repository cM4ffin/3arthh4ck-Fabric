package me.earth.earthhack.impl.commands;

import me.earth.earthhack.api.command.Command;
import me.earth.earthhack.api.command.PossibleInputs;
import me.earth.earthhack.api.util.interfaces.Globals;
import me.earth.earthhack.impl.commands.util.CommandDescriptions;
import me.earth.earthhack.impl.util.text.ChatUtil;
import me.earth.earthhack.impl.util.text.TextColor;
import net.minecraft.entity.Entity;

public class HClipCommand extends Command implements Globals
{
    public HClipCommand()
    {
        super(new String[][]{{"hclip"}, {"amount"}});
        CommandDescriptions.register(this, "Teleports you horizontally.");
    }

    @Override
    public void execute(String[] args)
    {
        if (args.length == 1)
        {
            ChatUtil.sendMessage(TextColor.RED
                    + "Please specify an amount to be teleported by.", getName());
            return;
        }

        if (mc.player == null)
        {
            ChatUtil.sendMessage(TextColor.RED
                    + "You need to be ingame to use this command.", getName());
            return;
        }

        try
        {
            double h = Double.parseDouble(args[1]);
            Entity entity = mc.player.getVehicle() != null
                                ? mc.player.getVehicle()
                                : mc.player;
            double yaw =
                    Math.cos(Math.toRadians(mc.player.getYaw() + 90.0f));
            double pit =
                    Math.sin(Math.toRadians(mc.player.getYaw() + 90.0f));

            entity.setPosition(
                entity.getX() + h * yaw, entity.getY(), entity.getZ() + h * pit);
            // PacketUtil.doPosition(mc.player.posX + h * yaw, mc.player.posY, mc.player.posZ + h * pit, mc.player.onGround);

            ChatUtil.sendMessage(TextColor.GREEN
                                    + "HClipped you "
                                    + TextColor.WHITE
                                    + args[1]
                                    + TextColor.GREEN
                                    + " blocks.", getName());
        }
        catch (Exception e)
        {
            ChatUtil.sendMessage(TextColor.RED
                    + "Couldn't parse "
                    + TextColor.WHITE
                    + args[1]
                    + TextColor.RED
                    + ", a number (can be a floating point one) is required.", getName());
        }
    }

    @Override
    public PossibleInputs getPossibleInputs(String[] args)
    {
        if (args.length > 1)
        {
            return PossibleInputs.empty();
        }

        return super.getPossibleInputs(args);
    }

}
