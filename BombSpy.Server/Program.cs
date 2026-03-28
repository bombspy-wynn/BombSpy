using System;
using System.Text.Json;
using BombSpy.Server.Auth;
using BombSpy.Server.Config;
using BombSpy.Server.Registries;
using BombSpy.Server.Services;
using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.OpenApi.Models;

var builder = WebApplication.CreateBuilder(args);


// --- "Database" ---
builder.Services.AddSingleton<ContributorConfig>(_ => new ContributorConfig(Environment.GetEnvironmentVariable("CONTRIBUTORS_JSON_PATH") ?? "contributors.json"));
builder.Services.AddSingleton<BombRegistry>();


// --- API ---
builder.Services.AddScoped<ContributorAuthFilter>();
builder.Services.AddSingleton<BombNotificationService>();
builder.Services.AddControllers().AddJsonOptions(options => { options.JsonSerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase; });

builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowEverything",
        policy =>
        {
            policy.AllowAnyOrigin()
                .AllowAnyHeader()
                .AllowAnyMethod();
        });
});


// --- Frontend ---
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "BombSpy API",
        Version = "v1"
    });
    
    c.AddSecurityDefinition("X-API-Key",new OpenApiSecurityScheme
    {
        Name = "X-API-Key",
        Type = SecuritySchemeType.ApiKey,
        Scheme = "ApiKeyScheme",
        In = ParameterLocation.Header,
        Description = "ApiKey must appear in header"
    });
    
    c.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = "X-API-Key"
                },
                In = ParameterLocation.Header
            },
            new string[]{}
        }
    });
});


// --- App ---
var app = builder.Build();

if (app.Environment.IsDevelopment()) app.UseDeveloperExceptionPage();

app.UseHttpsRedirection();

app.UseRouting();

app.UseCors("AllowEverything");

app.UseWebSockets();

app.MapControllers();
app.UseSwagger();
app.UseSwaggerUI();

app.Run();
