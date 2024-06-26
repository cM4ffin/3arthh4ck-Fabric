package me.earth.earthhack.impl.modules.combat.criticals;

import me.earth.earthhack.api.module.data.DefaultData;

final class CriticalsData extends DefaultData<Criticals>
{
    public CriticalsData(Criticals module)
    {
        super(module);
        register(module.delay, """
                - Packet : Will deal Critical hits silently.\s
                - Bypass : Is like packet but works on 2b2t.\s
                - Jump : Will automatically jump. (Only use this on your client, not on the PingBypass)\s
                 -MiniJump Will jump as well but less. (Only use this on your client, not on the PingBypass)""");
        register(module.noDesync, "Will not deal Critical hits against crystals " +
                "which would otherwise make your AutoCrystal desync you.");
        register(module.movePause, "Will not deal criticals while you're moving");
        register(module.delay, "The Delay in milliseconds between 2 criticals."
                + " Higher delays prevent desync even further.");
    }

    @Override
    public String getDescription()
    {
        return "Automatically deals critical hits when attacking Entities.";
    }

}
