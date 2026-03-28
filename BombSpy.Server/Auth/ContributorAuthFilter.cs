using BombSpy.Server.Config;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.Extensions.DependencyInjection;

namespace BombSpy.Server.Auth;

public class ContributorAuthFilter : IAuthorizationFilter
{
    private const string ApiKeyHeaderName = "X-API-Key";

    public void OnAuthorization(AuthorizationFilterContext context)
    {
        if (!context.HttpContext.Request.Headers.TryGetValue(ApiKeyHeaderName, out var apiKey))
        {
            context.Result = new UnauthorizedObjectResult("API Key missing");
            return;
        }

        ContributorConfig contributorConfig = context.HttpContext.RequestServices.GetRequiredService<ContributorConfig>();

        if (!contributorConfig.IsValid(apiKey))
        {
            context.Result = new UnauthorizedObjectResult("Invalid API Key");
            return;
        }
    }
}
