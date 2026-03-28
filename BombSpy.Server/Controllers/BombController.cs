using System.Linq;
using System.Net.WebSockets;
using System.Threading.Tasks;
using BombSpy.Server.Auth;
using BombSpy.Server.Models;
using BombSpy.Server.Registries;
using BombSpy.Server.Services;
using Microsoft.AspNetCore.Mvc;

namespace BombSpy.Server.Controllers;

[ApiController]
[Route("")]
public class BombController(BombRegistry bombRegistry, BombNotificationService bombNotificationService) : ControllerBase
{
    [HttpGet]
    public async Task<IActionResult> Get()
    {
        if (HttpContext.WebSockets.IsWebSocketRequest)
        {
            WebSocket socket = await HttpContext.WebSockets.AcceptWebSocketAsync();
            await bombNotificationService.HandleClientAsync(socket);
            return new EmptyResult();
        }
        
        bombRegistry.Clean();
        
        return Ok(bombRegistry.Bombs.Values.ToList());
    }

    [HttpPut]
    [ContributorEndpoint]
    public async Task<IActionResult> Put([FromBody] Bomb bomb)
    {
        var bombKey = new BombKey
        {
            Type = bomb.Type,
            Server = bomb.Server
        };
        
        bombRegistry.Clean();
        bombRegistry.Bombs.TryGetValue(bombKey, out Bomb? existingBomb);
        
        if (bomb.IsProbably(existingBomb)) return Ok(existingBomb);

        bombRegistry.Bombs[bombKey] = bomb;

        await bombNotificationService.NotifyBombThrownAsync(bomb);

        return Ok(bomb);
    }
}
