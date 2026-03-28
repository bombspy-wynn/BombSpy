using System;
using BombSpy.Server.Models;

namespace BombSpy.Server.Mappers;

public static class Mappers
{
    public static TimeSpan GetDuration(this BombType bombType)
    {
        return bombType switch
        {
            BombType.CombatXP or BombType.Loot or BombType.ProfessionXP or BombType.LootChest => TimeSpan.FromMinutes(20),
            BombType.ProfessionSpeed or BombType.Dungeon => TimeSpan.FromMinutes(10),
            _ => throw new ArgumentOutOfRangeException(nameof(bombType), bombType, null)
        };
    }
}
