using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.Json;
using System.Threading;
using BombSpy.Server.Models;

namespace BombSpy.Server.Config;

public class ContributorConfig
{
    private static readonly JsonSerializerOptions SerializerOptions = new() { PropertyNamingPolicy = JsonNamingPolicy.CamelCase };
    
    private List<Contributor> _keys = [];
    private readonly FileSystemWatcher _watcher;

    public ContributorConfig(string path)
    {
        Reload(path);

        _watcher = new FileSystemWatcher(Path.GetDirectoryName(path)!, Path.GetFileName(path))
        {
            NotifyFilter = NotifyFilters.LastWrite,
            EnableRaisingEvents = true
        };
        _watcher.Changed += (_, _) => Reload(path);
    }

    private void Reload(string path)
    {
        const int maxRetries = 5;
        const int delayMs = 100;

        for (int i = 0; i < maxRetries; i++)
        {
            try
            {
                var json = File.ReadAllText(path);
                _keys = JsonSerializer.Deserialize<List<Contributor>>(json, SerializerOptions) ?? [];
                return;
            }
            catch (IOException) when (i < maxRetries - 1)
            {
                Thread.Sleep(delayMs);
            }
        }
    }

    public bool IsValid(string key) => _keys.Any(k => k.ApiKey == key);
}
