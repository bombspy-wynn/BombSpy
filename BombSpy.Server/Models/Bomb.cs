using System;
using BombSpy.Server.Mappers;

namespace BombSpy.Server.Models;

public class Bomb
{
    public string Thrower { get; set; }
    public BombType Type { get; set; }
    public required string Server { get; set; }
    public DateTime StartTime { get; set; }
    public DateTime EndTime => StartTime + Type.GetDuration();

    public bool IsProbably(Bomb? bomb)
    {
        if (bomb == null) return false;
        if (bomb.Thrower != Thrower) return false;
        if (bomb.Type != Type) return false;
        if (bomb.Server != Server) return false;
        if ((bomb.StartTime - StartTime).Duration() > TimeSpan.FromSeconds(30)) return false;

        return true;
    }

    public bool IsActive()
    {
        return EndTime > DateTime.UtcNow;
    }
}
