using Microsoft.AspNetCore.Mvc;

namespace BombSpy.Server.Auth;

public class ContributorEndpointAttribute() : ServiceFilterAttribute(typeof(ContributorAuthFilter));
