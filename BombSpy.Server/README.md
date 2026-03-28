# BombSpy Server

## Building

### Prerequisites

.NET 9

### Commands

The build process is standard as hell just use `dotnet publish`.
You can also add either `-r win-x64` or `-r linux-x64` if you wanna specifically publish for either of those runtimes.
The resulting executable and `contributors.json` config can be found in `bin/Release/net9.0/`.
If you didn't specify a runtime, it will just be in `bin/Release/net9.0/publish/`.
If you did specify one, it will be in `bin/Release/net9.0/YOUR_RUNTIME_HERE/publish/`

## Usage

Deadass just run the executable and you're set. To add a contributor, edit `contributors.json`.
It already has an example contributor so you know what format to use.
If you edit the file while the server is running, the server will detect it and update the contributors live. No restart required.
