using System;
using System.Collections.Generic;
using System.Net.WebSockets;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using BombSpy.Server.Models;

namespace BombSpy.Server.Services;

public class BombNotificationService
{
    private static readonly JsonSerializerOptions SerializerOptions = new() { PropertyNamingPolicy = JsonNamingPolicy.CamelCase };
    private readonly List<WebSocket> _clients = [];

    public async Task HandleClientAsync(WebSocket socket)
    {
        _clients.Add(socket);

        // Keep the socket alive until the client disconnects
        byte[] buffer = new byte[1024];
        while (socket.State == WebSocketState.Open)
        {
            WebSocketReceiveResult result;
            try
            {
                result = await socket.ReceiveAsync(buffer, CancellationToken.None);
                if (result.MessageType == WebSocketMessageType.Close) break;
            }
            catch (WebSocketException e)
            {
                break;
            }
        }

        _clients.Remove(socket);
        
        if (socket.State != WebSocketState.Aborted)
            await socket.CloseAsync(WebSocketCloseStatus.NormalClosure, "Closed", CancellationToken.None);
    }

    public async Task NotifyBombThrownAsync(Bomb bomb)
    {
        string json = JsonSerializer.Serialize(bomb, SerializerOptions);
        byte[] bytes = Encoding.UTF8.GetBytes(json);
        var segment = new ArraySegment<byte>(bytes);

        foreach (WebSocket socket in _clients)
        {
            if (socket.State != WebSocketState.Open) continue;
            await socket.SendAsync(segment, WebSocketMessageType.Text, true, CancellationToken.None);
        }
    }
}
