using System.Collections.Generic;
using BombSpy.Server.Models;

namespace BombSpy.Server.Registries;

public class BombRegistry
{
    public Dictionary<BombKey, Bomb> Bombs = new();

    public void Clean()
    {
        foreach(BombKey bombKey in Bombs.Keys)
            if (!Bombs[bombKey].IsActive())
                Bombs.Remove(bombKey);
    }
}
